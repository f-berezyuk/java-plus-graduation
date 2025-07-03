package ru.practicum.stats.analyzer.handler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.stats.analyzer.model.EventSimilarity;
import ru.practicum.stats.analyzer.model.UserAction;
import ru.practicum.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.stats.analyzer.repository.UserActionRepository;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationsHandlerImpl implements RecommendationsHandler {

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        Long userId = request.getUserId();
        int maxResults = request.getMaxResult();

        List<UserAction> userActions = userActionRepository.findAllByUserId(userId,
                PageRequest.of(0, maxResults, Sort.by(Sort.Direction.DESC, "timestamp")));
        if (userActions.isEmpty()) {
            return List.of();
        }

        List<EventSimilarity> eventSimilarities = eventSimilarityRepository.findAllByEventAIn(userActions.stream()
                        .map(UserAction::getEventId)
                        .collect(Collectors.toSet()),
                PageRequest.of(0, maxResults, Sort.by(Sort.Direction.DESC, "score")));
        List<EventSimilarity> eventSimilaritiesB = eventSimilarityRepository.findAllByEventBIn(userActions.stream()
                        .map(UserAction::getEventId)
                        .collect(Collectors.toSet()),
                PageRequest.of(0, maxResults, Sort.by(Sort.Direction.DESC, "score")));

        List<Long> newEventIdsA = eventSimilarities.stream()
                .map(EventSimilarity::getEventB)
                .filter(eventId -> !userActionRepository.existsByEventIdAndUserId(eventId, userId))
                .distinct()
                .toList();

        List<Long> newEventIdsB = eventSimilaritiesB.stream()
                .map(EventSimilarity::getEventA)
                .filter(eventId -> !userActionRepository.existsByEventIdAndUserId(eventId, userId))
                .distinct()
                .toList();

        Set<Long> newEventIds = new HashSet<>(newEventIdsA);
        newEventIds.addAll(newEventIdsB);

        return newEventIds.stream()
                .map(eId -> RecommendedEventProto.newBuilder()
                        .setEventId(eId)
                        .setScore(calculateScore(eId, userId, maxResults))
                        .build())
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .limit(maxResults)
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        Long eventId = request.getEventId();
        Long userId = request.getUserId();

        List<EventSimilarity> eventSimilaritiesA = eventSimilarityRepository.findAllByEventA(eventId,
                PageRequest.of(0, request.getMaxResult(), Sort.by(Sort.Direction.DESC, "score")));
        List<EventSimilarity> eventSimilaritiesB = eventSimilarityRepository.findAllByEventB(eventId,
                PageRequest.of(0, request.getMaxResult(), Sort.by(Sort.Direction.DESC, "score")));

        List<RecommendedEventProto> recommendations = new ArrayList<>(eventSimilaritiesA.stream()
                .filter(es -> !userActionRepository.existsByEventIdAndUserId(es.getEventB(), userId))
                .map(es -> RecommendedEventProto.newBuilder()
                        .setEventId(es.getEventB())
                        .setScore(es.getScore())
                        .build())
                .toList());

        List<RecommendedEventProto> recommendationsB = eventSimilaritiesB.stream()
                .filter(es -> !userActionRepository.existsByEventIdAndUserId(es.getEventA(), userId))
                .map(es -> RecommendedEventProto.newBuilder()
                        .setEventId(es.getEventA())
                        .setScore(es.getScore())
                        .build())
                .toList();

        recommendations.addAll(recommendationsB);

        return recommendations.stream()
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .limit(request.getMaxResult())
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        return new ArrayList<>(request.getEventIdList().stream()
                .map(eId -> RecommendedEventProto.newBuilder()
                        .setEventId(eId)
                        .setScore(userActionRepository.getSumWeightByEventId(eId))
                        .build())
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .toList());
    }

    private float calculateScore(Long eventId, Long userId, int limit) {
        List<EventSimilarity> eventSimilaritiesA = eventSimilarityRepository.findAllByEventA(eventId,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score")));

        List<EventSimilarity> eventSimilaritiesB = eventSimilarityRepository.findAllByEventB(eventId,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score")));

        Map<Long, Double> viewedEventScores = eventSimilaritiesA.stream()
                .filter(es -> userActionRepository.existsByEventIdAndUserId(es.getEventB(), userId))
                .collect(Collectors.toMap(EventSimilarity::getEventB, EventSimilarity::getScore));

        Map<Long, Double> viewedEventScoresB = eventSimilaritiesB.stream()
                .filter(es -> userActionRepository.existsByEventIdAndUserId(es.getEventA(), userId))
                .collect(Collectors.toMap(EventSimilarity::getEventA, EventSimilarity::getScore));

        viewedEventScores.putAll(viewedEventScoresB);

        Map<Long, Float> actionMarks = userActionRepository.findAllByEventIdInAndUserId(viewedEventScores.keySet(),
                        userId).stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getMark));

        Float sumWeightedMarks = ((Double) viewedEventScores.entrySet().stream()
                .map(entry -> actionMarks.get(entry.getKey()) * entry.getValue())
                .mapToDouble(Double::doubleValue).sum())
                .floatValue();

        Float sumScores = ((Double) viewedEventScores.values().stream().mapToDouble(Double::doubleValue).sum())
                .floatValue();

        return sumWeightedMarks / sumScores;
    }
}

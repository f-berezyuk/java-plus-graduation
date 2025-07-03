package ru.practicum.stats.client;

import java.time.Instant;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

@Component
public class CollectorClient {
    private final UserActionControllerGrpc.UserActionControllerBlockingStub userActionStub;

    public CollectorClient(@GrpcClient("collector") UserActionControllerGrpc.UserActionControllerBlockingStub client) {
        this.userActionStub = client;
    }

    public void collectUserAction(Long eventId, Long userId, ActionTypeProto type, Instant instant) {
        UserActionProto request = UserActionProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setActionType(type)
                .setTimestamp(mapToTimestamp(instant))
                .build();

        userActionStub.collectUserAction(request);
    }

    private Timestamp mapToTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
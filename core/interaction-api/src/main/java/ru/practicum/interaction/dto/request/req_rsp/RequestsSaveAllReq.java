package ru.practicum.interaction.dto.request.req_rsp;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.interaction.dto.request.RequestDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestsSaveAllReq {
    private List<RequestDto> requests;
}

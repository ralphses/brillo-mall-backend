package com.clickstechnology.Brillo.Mall.domain.log;

import com.clickstechnology.Brillo.Mall.application.api.contracts.RequestLogService;
import com.clickstechnology.Brillo.Mall.application.enums.RequestStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
class AllRequestService implements RequestLogService {

    private final AllRequestRepository allRequestRepository;
    private final ObjectMapper objectMapper;

    @Override
    public String logRequest(Object request, String origin, String userDevice) {
        try {
            AllRequest newRequest = AllRequest.builder()
                    .requestBody(objectMapper.writeValueAsString(request))
                    .origin(origin)
                    .status(RequestStatus.INITIATED)
                    .build();
            AllRequest saved = allRequestRepository.save(newRequest);
            return saved.getReference();
        }catch (Exception exception) {
            log.error("An error occurred while trying to save the request", exception);
        }
        return null;
    }

    @Override
    public void update(String logId, Object response, RequestStatus status) {
        allRequestRepository.findByReference(logId).ifPresent(allRequest -> {
            try {
                allRequest.setStatus(status);
                allRequest.setRequestBody(objectMapper.writeValueAsString(response));
                allRequest.setRespondedAt(LocalDateTime.now());
                allRequestRepository.save(allRequest);
            }catch (Exception exception) {
                log.error(":::An error occurred while trying to update the request", exception);
            }
        });
    }
}

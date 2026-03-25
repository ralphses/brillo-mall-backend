package com.clickstechnology.Brillo.Mall.domain.log;

import com.clickstechnology.Brillo.Mall.application.api.contracts.RequestLogService;
import com.clickstechnology.Brillo.Mall.application.enums.RequestStatus;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.RequestLogPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
class AllRequestService implements RequestLogService {

    private final AllRequestRepository allRequestRepository;

    @Override
    public String logRequest(String request, String origin, String userDevice) {
        try {
            AllRequest newRequest = AllRequest.builder()
                    .requestBody(request) // ✅ already JSON
                    .origin(origin)
                    .status(RequestStatus.INITIATED)
                    .build();

            AllRequest saved = allRequestRepository.save(newRequest);
            return saved.getReference();

        } catch (Exception exception) {
            log.error("An error occurred while trying to save the request", exception);
        }
        return null;
    }

    @Override
    public void update(String logId, String response, RequestStatus status) {
        allRequestRepository.findByReference(logId).ifPresent(allRequest -> {
            try {
                allRequest.setStatus(status);
                allRequest.setResponseBody(response); // ✅ FIX: don't overwrite request
                allRequest.setRespondedAt(LocalDateTime.now());
                allRequestRepository.save(allRequest);

            } catch (Exception exception) {
                log.error(":::An error occurred while trying to update the request", exception);
            }
        });
    }

    @Override
    public void saveAll(List<RequestLogPayload> logs) {
        List<AllRequest> allRequests = logs.stream()
                .filter(requestLogPayload -> Objects.nonNull(requestLogPayload.getRequestBody()))
                .map(this::toEntity)
                .toList();
        allRequestRepository.saveAll(allRequests);
    }

    private AllRequest toEntity(RequestLogPayload payload) {
        return AllRequest.builder()
                .requestBody(payload.getRequestBody())
                .responseBody(payload.getResponseBody())
                .origin(payload.getIpAddress())
                .status(payload.getStatus())
                .respondedAt(
                        payload.getStatus() == RequestStatus.PROCESSED ||
                                payload.getStatus() == RequestStatus.FAILED
                                ? LocalDateTime.now()
                                : null
                )
                .build();
    }


}

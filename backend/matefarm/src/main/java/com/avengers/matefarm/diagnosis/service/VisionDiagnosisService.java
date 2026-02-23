package com.avengers.matefarm.diagnosis.service;

import com.avengers.matefarm.diagnosis.dto.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Service
public class VisionDiagnosisService {

    private final WebClient fastApiWebClient;

    public VisionDiagnosisService(@Qualifier("fastApiWebClient") WebClient fastApiWebClient) {
        this.fastApiWebClient = fastApiWebClient;
    }

    public VisionDiagnosisResponse diagnose(Integer cropId, MultipartFile image, Integer topK) {

        MultiValueMap<String, Object> multipart = new LinkedMultiValueMap<>();
        multipart.add("crop_id", cropId);
        multipart.add("top_k", topK);
        multipart.add("image", buildFilePart(image));

        FastApiResponse fast = fastApiWebClient.post()
                .uri("/analyze")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipart))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new RuntimeException("FastAPI error: " + body))
                        )
                )
                .bodyToMono(FastApiResponse.class)
                .block();

        if (fast == null) throw new IllegalStateException("FastAPI returned null response");

        return toFrontendResponse(fast, cropId);
    }

    private HttpEntity<Resource> buildFilePart(MultipartFile file) {
        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return (file.getOriginalFilename() != null) ? file.getOriginalFilename() : "image.jpg";
                }
            };

            HttpHeaders headers = new HttpHeaders();
            String ct = (file.getContentType() != null) ? file.getContentType() : "image/jpeg";
            headers.setContentType(MediaType.parseMediaType(ct));
            headers.setContentDispositionFormData("image", resource.getFilename());

            return new HttpEntity<>(resource, headers);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }

    private VisionDiagnosisResponse toFrontendResponse(FastApiResponse fast, Integer requestCropId) {
        String modelKey = (fast.finalSection() != null && fast.finalSection().targetModel() != null)
                ? fast.finalSection().targetModel()
                : fast.targetModel();

        BestResponse best = null;
        if (fast.finalSection() != null && fast.finalSection().raw() != null) {
            var raw = fast.finalSection().raw();
            best = new BestResponse(raw.label(), raw.labelKo(), raw.prob());
        } else if (fast.modelResult() != null && fast.modelResult().best() != null) {
            var b = fast.modelResult().best();
            best = new BestResponse(b.label(), b.labelKo(), b.prob());
        }

        List<BestResponse> topK = List.of();
        if (fast.modelResult() != null && fast.modelResult().topk() != null) {
            topK = fast.modelResult().topk().stream()
                    .map(x -> new BestResponse(x.label(), x.labelKo(), x.prob()))
                    .toList();
        }

        long inferenceMs = 0L;
        if (fast.meta() != null && fast.meta().latencyMsTotal() != null) {
            inferenceMs = fast.meta().latencyMsTotal();
        } else if (fast.modelResult() != null && fast.modelResult().meta() != null
                && fast.modelResult().meta().latencyMs() != null) {
            inferenceMs = fast.modelResult().meta().latencyMs();
        }

        return new VisionDiagnosisResponse(
                requestCropId,
                modelKey,
                best,
                topK,
                new MetaResponse(inferenceMs),
                fast.ragAnswer()
        );
    }
}
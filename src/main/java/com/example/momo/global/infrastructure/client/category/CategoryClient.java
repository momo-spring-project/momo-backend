package com.example.momo.global.infrastructure.client.category;

import java.time.Duration;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.momo.global.common.dto.ApiResponse;
import com.example.momo.global.infrastructure.client.category.dto.CategoryClientResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryClient {

	private final static String CATEGORY_SERVICE_BASE_URI = "/api/v2/categories";
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
	private final WebClient webClient;

	public CategoryClientResponseDto getCategory(Integer categoryId) {
		try {
			ApiResponse<CategoryClientResponseDto> response = webClient
				.get()
				.uri(CATEGORY_SERVICE_BASE_URI + "/{categoryId}", categoryId)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<CategoryClientResponseDto>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			if (response == null || !response.isSuccess()) {
				return null;
			}

			log.debug("카테고리 조회 성공: categoryId={}", categoryId);
			return response.getData();

		} catch (WebClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				log.debug("카테고리를 찾을 수 없습니다: categoryId={}", categoryId);
				return null;
			}

			log.error("카테고리 조회 실패: categoryId={}, status={}, error={}",
				categoryId, e.getStatusCode(), e.getMessage());
			throw new CategoryClientException("카테고리 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	public List<CategoryClientResponseDto> getCategories() {
		try {
			ApiResponse<List<CategoryClientResponseDto>> response = webClient
				.get()
				.uri(CATEGORY_SERVICE_BASE_URI)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<List<CategoryClientResponseDto>>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			if (response == null || !response.isSuccess()) {
				return null;
			}

			log.debug("카테고리 목록 조회 성공");
			return response.getData();

		} catch (WebClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				log.debug("카테고리 목록을 찾을 수 없습니다");
				return null;
			}

			log.error("카테고리 목록 조회 실패: status={}, error={}",
				e.getStatusCode(), e.getMessage());
			throw new CategoryClientException("카테고리 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}
}
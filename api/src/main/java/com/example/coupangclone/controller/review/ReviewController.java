package com.example.coupangclone.controller.review;

import com.example.coupangclone.dto.BasicResponseDto;
import com.example.coupangclone.dto.review.ReviewResponseDto;
import com.example.coupangclone.dto.review.ReviewSummaryResponseDto;
import com.example.coupangclone.dto.review.ReviewUpdateRequestDto;
import com.example.coupangclone.dto.review.ReviewWriteRequestDto;
import com.example.coupangclone.result.ReviewResult;
import com.example.coupangclone.security.userdetails.UserDetailsImpl;
import com.example.coupangclone.service.review.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@Tag(name = "리뷰 API", description = "상품 리뷰 작성, 조회, 삭제 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "구매(결제완료 이상)한 상품에 한해 리뷰를 작성합니다.")
    @PostMapping
    public ResponseEntity<ReviewResponseDto> writeReview(@Valid @RequestBody ReviewWriteRequestDto requestDto,
                                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ReviewResult result = reviewService.writeReview(requestDto.toCommand(), userDetails.getUser());
        return ResponseEntity.ok(ReviewResponseDto.from(result));
    }

    @Operation(summary = "상품 리뷰 목록 조회", description = "특정 상품의 리뷰 목록을 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<ReviewResponseDto>> getReviews(@RequestParam("itemId") Long itemId,
                                                                @PageableDefault(size = 10) Pageable pageable) {
        Page<ReviewResponseDto> response = reviewService.getReviews(itemId, pageable).map(ReviewResponseDto::from);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 리뷰 요약 조회", description = "특정 상품의 평균 평점과 리뷰 개수를 조회합니다.")
    @GetMapping("/summary")
    public ResponseEntity<ReviewSummaryResponseDto> getSummary(@RequestParam("itemId") Long itemId) {
        return ResponseEntity.ok(ReviewSummaryResponseDto.from(reviewService.getSummary(itemId)));
    }

    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰의 내용과 평점을 수정합니다.")
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(@PathVariable Long reviewId,
                                                            @Valid @RequestBody ReviewUpdateRequestDto requestDto,
                                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ReviewResult result = reviewService.updateReview(reviewId, requestDto.toCommand(), userDetails.getUser());
        return ResponseEntity.ok(ReviewResponseDto.from(result));
    }

    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<BasicResponseDto> deleteReview(@PathVariable Long reviewId,
                                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getUser());
        return ResponseEntity.ok(BasicResponseDto.addSuccess("리뷰가 삭제되었습니다."));
    }

}

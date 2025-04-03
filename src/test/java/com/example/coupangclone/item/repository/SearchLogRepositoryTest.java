package com.example.coupangclone.item.repository;

import com.example.coupangclone.item.entity.SearchLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SearchLogRepositoryTest {

    @Autowired
    private SearchLogRepository searchLogRepository;

    @DisplayName("키워드를 포함한 연관 검색어들을 조회한다.")
    @Test
    void findTopByKeywordContaining(){
        // given
        SearchLog log1 = createLog(List.of("노트북", "노트북pro", "냉장고"), "삼성", "삼성");
        SearchLog log2 = createLog(List.of("노트북, 노트북pro"), "노트북", "삼성");
        SearchLog log3 = createLog(List.of("노트북pro"), "pro", "삼성");
        searchLogRepository.saveAll(List.of(log1, log2, log3));

        // when
        List<String> result = searchLogRepository.findTopByKeywordContaining("노트북", 2);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo("노트북pro");
        assertThat(result).contains("노트북pro");
    }

    @DisplayName("조건에 맞는 키워드가 없으면 빈 리스트를 반환한다.")
    @Test
    void findTopByKeywordContaining_noMatch() {
        // given
        SearchLog log = SearchLog.builder()
                .mainKeyword("애플")
                .brand("애플")
                .keywords(List.of("아이폰", "맥북", "에어팟"))
                .build();

        searchLogRepository.save(log);

        // when
        List<String> result = searchLogRepository.findTopByKeywordContaining("애플워치", 5);

        // then
        assertThat(result).isEmpty();
    }

    private SearchLog createLog(List<String> keywords, String mainKeyword, String brand) {
        return SearchLog.builder()
                .keywords(keywords)
                .mainKeyword(mainKeyword)
                .brand(brand)
                .build();
    }

}
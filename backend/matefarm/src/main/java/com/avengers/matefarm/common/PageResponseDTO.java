package com.avengers.matefarm.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PageResponseDTO<T> {

    @JsonProperty("elements")
    private List<T> elements; // 요소 ( 게시글 갯수 )

    @JsonProperty("current_page")
    private Integer currentPage; // 현재 페이지 번호

    @JsonProperty("page_size")
    private Integer pageSize; // 보여줄 페이지 간격 ex) 1~10, 11~20

    @JsonProperty("elements_per_page")
    private Integer elementsPerPage; // 한 페이지에 보여줄 요소 개수

    @JsonProperty("total_elements")
    private Integer totalElements; // 전체 요소 개수

    @JsonProperty("start_page")
    private Integer startPage; // 시작 페이지 숫자

    @JsonProperty("end_page")
    private Integer endPage; // 끝 페이지 숫자

    @JsonProperty("prev")
    private Boolean prev; // start_page 이전 숫자 존재 여부

    @JsonProperty("next")
    private Boolean next; // end_page 다음 숫자 존재 여부


    public PageResponseDTO(List<T> elements,
                           Integer pageNo,
                           Integer pageSize,
                           Integer elementsPerPage,
                           Integer total){
        this.elements = elements;
        this.currentPage = pageNo;
        this.pageSize = pageSize;
        this.elementsPerPage = elementsPerPage;
        this.totalElements = total;

        /* endPage (화면 하단 끝 번호)
         * 현재 페이지를 elementsPerPage 크기로 나누고 올림한 뒤 다시 곱함
         * 예) 현재 13페이지고 10개씩 보여준다면 -> ceil(13/10) = 2 -> 2 * 10 = 20페이지
        * */
        this.endPage = (int)(Math.ceil((double)pageNo / pageSize)) * pageSize;

        /* endPage가 20이면 startPage는 11이 되어서 11~20 범위 결정 */
        this.startPage = this.endPage - pageSize + 1;

        /* realEnd :"전체 페이지 수"를 "페이지당 보여줄 게시글의 수"로 나누고 (int) 로 캐스팅
           게시글이 100 / 10 = 10 처럼 정확하게 나누어 떨어지지 않고
           107/10 . 10.7 등 소수로 나누어 떨어지는 경우를 고려하여
           소수점 아래 올림 처리를 해 실제 Page의 끝 번호를 리턴
           10.7의 경우 11로 반환.
        * */
        int realEnd = (int)(Math.ceil((double)total / elementsPerPage));

        /* endPage 보정 (중요!)
         * 계산된  끝 번호(20)가 실제 마지막 페이지(14)보다 크면,
         * 실제 마지막 페이지를 끝 번호로 설정하여 유령 페이지 버튼이 생기는 것을 방지함.
         * 예) 11~20 이면 endPage는 20이 되나 totalElements로 계산한 실제 realEnd가 12인 경우
         * 20번까지 페이지가 보이게 하지 않고 12번 까지만 보이도록 조정하겠다는 의미.
         * < |11| |12| > 이렇게.
         *
         * 왜 endPage와 realEnd를 나눠서 사용하냐면, endPage가 실제로 1000이라면 게시판 아래에 1~100까지를 전부 뿌려줘야 하기 때문에
         * 10개씩 반환해서 보여주려고 endPage를 10개씩 갯수를 나눠 논리적으로 보여줄 페이지 수를 정의하고
         * 데이터가 부족한 마지막 페이지만 realEnd로 보정을 하는 것.
         *
        * */
        if(realEnd < this.endPage){
            this.endPage = realEnd;
        }

        /* prev (이전 블록 존재 여부)
         * 시작 페이지가 1보다 크면 앞에 더 보여줄 페이지 블록이 있다는 뜻.
         * 이 값이 true면 프론트엔드에서 [이전] 버튼을 활성화함.
         * 예) 1이면 false, 11이면 true
        * */
        this.prev = this.startPage > 1;

        /* next (다음 블록 존재 여부)
         * 현재 블록의 끝 번호가 실제 마지막 페이지보다 작으면 뒤에 더 보여줄 페이지가 있다는 뜻.
         * 이 값이 true면 프론트엔드에서 [다음] 버튼을 활성화함.
         * 예) 11~20번 페이지를 보고 있는데 realEnd가 25라면 true
        * */
        this.next = this.endPage < realEnd;
    }

}


/*
  프론트엔드는 요소에 따른 로직을 구현하지 않고.

  1) elements로 목록을 그리고
  2) startPage부터 endPage까지 반복문 돌려서 숫자 버튼을 만들고
  3) prev/next 값으로 화살표만 띄우면 된다.

* */


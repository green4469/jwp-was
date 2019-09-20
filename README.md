# 웹 애플리케이션 서버
## 진행 방법
* 웹 애플리케이션 서버 요구사항을 파악한다.
* 요구사항에 대한 구현을 완료한 후 자신의 github 아이디에 해당하는 브랜치에 Pull Request(이하 PR)를 통해 코드 리뷰 요청을 한다.
* 코드 리뷰 피드백에 대한 개선 작업을 하고 다시 PUSH한다.
* 모든 피드백을 완료하면 다음 단계를 도전하고 앞의 과정을 반복한다.

## 우아한테크코스 코드리뷰
* [온라인 코드 리뷰 과정](https://github.com/woowacourse/woowacourse-docs/blob/master/maincourse/README.md)

## To-Do-List
- HttpRequest, HttpResponse 가 in/out stream 을 사용하지 않도록 리팩토링
    - in/out stream 에서 데이터를 읽거나 쓰는 책임은 HttpRequest / HttpResponse 가 갖는 것보다 별도의 클래스가 갖는 것이 낫다.
    - 점진적인 리팩토링을 통해 in/out 에서 문자열로 리팩토링한다.
        - 기존 코드를 유지한 채 리팩토링한 코드를 추가, 점진적으로 리팩토링한 코드를 사용하도록 기존 코드를 변경
    
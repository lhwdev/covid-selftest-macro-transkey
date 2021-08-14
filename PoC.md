# transkey 관련 PoC

## 참고

입력 타입이 [form](https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4)
(`Content-Type`이 `application/x-www-form-urlencoded`인 경우)일 때는 본문 내용을 보기 좋게 하기 위해 실제 form의 형태로 적지 않았습니다.

- HTTP 591: `WAF` 쿠키가 없을 때 (추정)
- HTTP 592: `_JSESSIONID` 쿠키가 없을 때(추정)

아래의 api 목록은 호출해야 하는 순서대로 나와 있습니다.

## transkey 시즌 생성 & 초기화

* transkey url: `https://hcs.eduro.go.kr/transkeyServlet`

### 기본 함수/변수들

- 함수 encryptRsa(A): 공개키(b)를 이용해 데이터를 RSA 알고리즘, OAEP 패딩(message digest가 SHA1, mask generation function이 MGF1)
  으로 암호화함

- sessionKey(a): 렌덤 데이터(8 byte)
- encryptedKey(b): sessionKey(a)를 hex 인코딩한 텍스트를 encryptRsa(A)한 것
- uuid(c): 렌덤 데이터(32 byte)를 hex 인코딩한 텍스트
- useAsyncTranskey(d): `transkey.js` 내부적으로 비동기 호출을 사용하는지 여부, `transkey.js` 안에 정의돼 있음

### 토큰 가져오기(r1)

* 주소: HTTP GET / `<transkey url>` + url 쿼리
* **url 쿼리**:
  - `op`: `getToken`
* 결과: js (지금까지는 고정값, 하드코딩 가능할지도)
  ```text
  var TK_requestToken=0;
  ```
* 패턴:
  - token(a)
    ```regexp
    var TK_requestToken=(.*);
    ```

### 여러 정보와 flag들 가져오기

* 주소: HTTP POST / `<transkey url>` + url 퀴리
* url 쿼리:
  - `op`: `getInitTime`
* 결과: js (여기서 개행 없이)
  ```js
  var initTime='de8ca386423880b46f63347dd990ccc5'; // initTime 값, useAsyncTranskey가 true라면 의미있음
  var limitTime=1;
  var useSession=false; // session 사용 여부
  var useSpace=true;
  var useGenKey=false;
  var useTalkBack=true;
  var java_ver=1.8;
  ```
  * 패턴:
    - decInitTime: (있을 수도, 없을 수도 있음)
      ```regexp
      var initTime='([0-9a-fA-F]*)';
      ```
    - initTime:
      ```regexp
      var initTime='([0-9a-fA-F]*)';
      ```
    - initTime:
      ```regexp
      var initTime='([0-9a-fA-F]*)';
      ```

### 인증서 가져오기(r2)

* 주소: HTTP POST / `<transkey url>`
* 헤더:
  - `Content-Type`: `application/x-www-form-urlencoded`
* **입력**: form
  ```yaml
  op: getPublicKey # 작업의 종류
  TK_requestToken: <token(a)>
  ```
* 결과: der 인코딩된 `X.509` 형태의 인증서
  - 공개키(b): 여기서 나온 publicKey

### 키 정보 가져오기(r3)

* 주소: HTTP POST / `<transkey url>`
* 헤더:
  - `Content-Type`: `application/x-www-form-urlencoded`
* **입력**: form
  ```yaml
  op: getKeyInfo
  key: <encryptedKey(b)>
  transkeyUuid: <uuid(c)>
  useCert: true
  TK_requestToken: <토큰(a)>
  mode: common
  ```
* 결과: js























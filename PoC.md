# ๐ง transkey ๊ด๋ จ PoC (๋ฏธ์์ฑ)

## ์ฐธ๊ณ 

์๋ ฅ ํ์์ด [form](https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4)
(`Content-Type`์ด `application/x-www-form-urlencoded`์ธ ๊ฒฝ์ฐ)์ผ ๋๋ ๋ณธ๋ฌธ ๋ด์ฉ์ ๋ณด๊ธฐ ์ข๊ฒ ํ๊ธฐ ์ํด ์ค์  form์ ํํ๋ก ์ ์ง ์์์ต๋๋ค.

- HTTP 591: `WAF` ์ฟ ํค๊ฐ ์์ ๋ (์ถ์ )
- HTTP 592: `_JSESSIONID` ์ฟ ํค๊ฐ ์์ ๋(์ถ์ )

์๋์ api ๋ชฉ๋ก์ ํธ์ถํด์ผ ํ๋ ์์๋๋ก ๋์ ์์ต๋๋ค.

## transkey ์์ฆ ์์ฑ & ์ด๊ธฐํ

* transkey url: `https://hcs.eduro.go.kr/transkeyServlet`

### ๊ธฐ๋ณธ ํจ์/๋ณ์๋ค

- ํจ์ encryptRsa(A): ๊ณต๊ฐํค(b)๋ฅผ ์ด์ฉํด ๋ฐ์ดํฐ๋ฅผ RSA ์๊ณ ๋ฆฌ์ฆ, OAEP ํจ๋ฉ(message digest๊ฐ SHA1, mask generation function์ด MGF1)
  ์ผ๋ก ์ํธํํจ

- sessionKey(a): ๋ ๋ค ๋ฐ์ดํฐ(8 byte)
- encryptedKey(b): sessionKey(a)๋ฅผ hex ์ธ์ฝ๋ฉํ ํ์คํธ๋ฅผ encryptRsa(A)ํ ๊ฒ
- uuid(c): ๋ ๋ค ๋ฐ์ดํฐ(32 byte)๋ฅผ hex ์ธ์ฝ๋ฉํ ํ์คํธ
- useAsyncTranskey(d): `transkey.js` ๋ด๋ถ์ ์ผ๋ก ๋น๋๊ธฐ ํธ์ถ์ ์ฌ์ฉํ๋์ง ์ฌ๋ถ, `transkey.js` ์์ ์ ์๋ผ ์์

### ํ ํฐ ๊ฐ์ ธ์ค๊ธฐ(r1)

* ์ฃผ์: HTTP GET / `<transkey url>` + url ์ฟผ๋ฆฌ
* **url ์ฟผ๋ฆฌ**:
  - `op`: `getToken`
* ๊ฒฐ๊ณผ: js (์ง๊ธ๊น์ง๋ ๊ณ ์ ๊ฐ, ํ๋์ฝ๋ฉ ๊ฐ๋ฅํ ์ง๋)
  ```text
  var TK_requestToken=0;
  ```
* ํจํด:
  - token(a)
    ```regexp
    var TK_requestToken=(.*);
    ```

### ์ฌ๋ฌ ์ ๋ณด์ flag๋ค ๊ฐ์ ธ์ค๊ธฐ

* ์ฃผ์: HTTP POST / `<transkey url>` + url ํด๋ฆฌ
* url ์ฟผ๋ฆฌ:
  - `op`: `getInitTime`
* ๊ฒฐ๊ณผ: js (์ฌ๊ธฐ์ ๊ฐํ ์์ด)
  ```js
  var initTime='de8ca386423880b46f63347dd990ccc5'; // initTime ๊ฐ, useAsyncTranskey๊ฐ true๋ผ๋ฉด ์๋ฏธ์์
  var limitTime=1;
  var useSession=false; // session ์ฌ์ฉ ์ฌ๋ถ
  var useSpace=true;
  var useGenKey=false;
  var useTalkBack=true;
  var java_ver=1.8;
  ```
  * ํจํด:
    - decInitTime: (์์ ์๋, ์์ ์๋ ์์)
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

### ์ธ์ฆ์ ๊ฐ์ ธ์ค๊ธฐ(r2)

* ์ฃผ์: HTTP POST / `<transkey url>`
* ํค๋:
  - `Content-Type`: `application/x-www-form-urlencoded`
* **์๋ ฅ**: form
  ```yaml
  op: getPublicKey # ์์์ ์ข๋ฅ
  TK_requestToken: <token(a)>
  ```
* ๊ฒฐ๊ณผ: der ์ธ์ฝ๋ฉ๋ `X.509` ํํ์ ์ธ์ฆ์
  - ๊ณต๊ฐํค(b): ์ฌ๊ธฐ์ ๋์จ publicKey

### ํค ์ ๋ณด ๊ฐ์ ธ์ค๊ธฐ(r3)

* ์ฃผ์: HTTP POST / `<transkey url>`
* ํค๋:
  - `Content-Type`: `application/x-www-form-urlencoded`
* **์๋ ฅ**: form
  ```yaml
  op: getKeyInfo
  key: <encryptedKey(b)>
  transkeyUuid: <uuid(c)>
  useCert: true
  TK_requestToken: <ํ ํฐ(a)>
  mode: common
  ```
* ๊ฒฐ๊ณผ: js























척하면 척! 척척이
=================
ETRI 오픈 API 활용 공모전
-------------------------
### 1. 프로젝트 소개
코로나19 이후 비대면 시대가 도래하면서, 다양한 업종에서 키오스크를 이용한 비대면 거래방식을 도입하고 있다. 하지만 소비자24의 설문조사에  따르면, 소비자의 46.6%가 키오스크 이용 중 불편한 경험이 있었다고 응답했다. 특히 노년층 등 디지털 소외계층은 디지털 기기를 통한 주문방식에 더 큰 어려움을 겪고 있다. 이러한 상황에서, 우리는 그들의 비대면 주문을 돕기 위하여 ETRI OPEN API를 활용하여 음성인식 방식을 도입한 키오스크 어플리케이션을 개발하였다.
<div>
<img width="135" alt="image" src="https://github.com/KNUwarriors/ETRI_Kiosk/assets/87633056/769a37c5-1afd-4b25-a650-c547ddd67282">
</div>


### 2. 프로젝트 기능
<details>
  <summary>메인 화면</summary>
    <img width="162" alt="image" src="https://github.com/KNUwarriors/ETRI_Kiosk/assets/87633056/37c7999e-e510-4f1f-8499-94fbc98389be">
    <div>
    <ul>
      <li>메뉴판: 카페 메뉴판을 Recycler View를 사용하여 화면에 출력</li>
      <li>주문하기 버튼: 주문페이지로 이동하는 버튼</li>
      <li>메뉴 사전: 메뉴를 검색하는 메뉴 사전 페이지로 이동하는 버튼</li>
     <li>결제하기: 결제를 하여 장바구니를 비워주는 버튼</li>
     <li>장바구니: 사용자가 주문한 메뉴들을 Recycler View를 통해 결제버튼 왼 편에 출력한다. ‘+’버튼과 ‘-’버튼을 이용하여 주문한 메뉴의 개수를 수정할 수 있다.</li>
    </ul>
  </div>
</details>

<details>
  <summary>주문 화면</summary>
  <img width="162" alt="image" src="https://github.com/KNUwarriors/ETRI_Kiosk/assets/87633056/4d5ca77a-9851-4df3-a4d3-0339ccd3abf8">
  <div>
    <ul>
      <li>사용자의 주문을 받아 장바구니에 추가하는 페이지이다.</li>
      <li>사용자의 음성인식 (STT: Speak To Text)을 통해 주문 메시지(예: 아메리카노 한잔 주세요)를 입력받는다.</li>
      <li>ETRI 기계독해 API를 사용하여 사용자의 주문메시지에서 ‘메뉴’를 추출한다. (예: ‘아메리카노 한잔 주세요’ -> ‘아메리카노’ 추출)</li>
      <li>주문이 확정되면 firebase의 DB에 실시간으로 주문을 추가하여 장바구니를 업데이트한다. 이때 업데이트된 장바구니 목록은 메인 화면에도 반영된다.</li>
      <li>주문이 확정되지 않으면 주문단계(음성인식으로 주문 메시지를 입력받는 단계)로 되돌아간다. 이때 주문이 10번이상 확정되지 않으면 주문페이지는 자동으로 닫히고 메인페이지로 돌아가게된다.</li>
      <li>‘X’ 버튼을 통해 주문 페이지에서 나갈 수 있다.</li>
    </ul>
  </div>
</details>
 
<details>
  <summary>메뉴 사전 화면</summary>
  <img width="162" alt="image" src="https://github.com/KNUwarriors/ETRI_Kiosk/assets/87633056/9838b9cd-37f1-4d9f-b191-64e08f30bff4">
  <div>
    <ul>
      <li>사용자가 메뉴에 대한 설명을 얻을 수 있는 페이지이다.</li>
      <li>사용자의 음성인식(STT)을 통해 메뉴와 관련된 질문을 입력받는다.(예: 아메리카노가 뭐야?)</li>
      <li>ETRI 위키백과 API를 사용하여 사용자의 질문에대한 위키백과 검색 결과를 받아. 메뉴사전페이지에 하단부분에 출력한다.</li>
      <li>‘X’ 버튼을 통해 메뉴사전 페이지에서 나갈 수 있다.</li>
    </ul>
  </div>
</details>

### 3. 기술 스택
<img width="629" alt="image" src="https://github.com/KNUwarriors/ETRI_Kiosk/assets/87633056/f52117aa-267a-4853-807d-0f8e7b983c19">


### 4. 워크플로우
<img width="629" alt="image" src="https://github.com/KNUwarriors/ETRI_Kiosk/assets/87633056/75955cbd-3319-494a-8e66-b7310b2522a5">


### 5. 실행 화면
클릭 시 유튜브 링크로 이동!
[![Video Label](http://img.youtube.com/vi/mKxJq0bmmcg/0.jpg)](https://youtu.be/mKxJq0bmmcg?t=0s)

### 6. 성과
ETRI(한국전자통신연구원) 오픈 API 활용 공모전 가작 수상

Android 12 
API 31 사용 권장

<마이크 사용>
실행 후 마이크 사용 허용 클릭 
Emulator 설정 (Extended controls, 점세개모양 버튼) -> virtual microphone uses input 허용 

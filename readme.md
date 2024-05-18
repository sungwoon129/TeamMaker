## Spring WebSocket STOMP 를 활용한 경매 웹 어플리케이션(Team Maker) ##

### 개요 ###
소수의 인원(평균적으로 2~10)이 한 공간에 모여, 경매시스템을 통해 물품 혹은 팀원을 자신이 가진 재화를 소비해 좋은 팀 혹은 구성을 만드는 애플리케이션

물품 혹은 팀원(이하 아이템)들이 랜덤한 순서로 경매에 나오고, 참가자들은 자신이 가진 재화를 효율적으로 사용해 다른 참가자들보다 좋은 팀 혹은 구성을 만드는 것이 목적.

경매과정에서 소켓을 통해 양방향 통신을 가능토록하여 실시간 경매가 진행되도록 구현 


### 환경 ###

> SpringBoot : 3.2.1  
> Gradle : 8.5  
> Socket : SockJS(1.0.2) + STOMP Web Socket(2.3.3)  
> JPA(Hibernate) : 6.4.1    
> DB : H2 Database, MongoDB(입찰, 낙찰, 유찰, 경매순서등의 경매진행중 발생하는 데이터관리)

### 동작 흐름(구현 계획) ###

> * 1차 경매 : 모든 경매 대상에 대해 실시. 여기서 입찰을 받지 못한 경매대상은 2차 경매 대상에 포함된다.
> * 2차 경매 : 1차경매에서 유찰된 대상들에 한해 2차 경매 실시. 1차 경매에서 모든 포지션을 낙찰받지 못한 경매참가자는 여기서 포지션을 채운다.  
  
<br>

<b> 1. 방 생성 ~ 경매 시작 전 </b>

방(채널) 개설 -> 참가자 전원 준비 상태 -> 방장이 경매 시작

<b> 2. 경매 시작 ~ 1차 경매 종료 </b>   <- 현재 1차 경매의 '낙찰/유찰 판단'까지 구현완료 

경매 대상 stage 올림 -> 하이라이트 영상 재생 -> 입찰 전 대기시간 -> 입찰 시작 -> 입찰 종료 -> <strong>낙찰/유찰 판단 후 데이터 최신화</strong>
-> 다음 경매대상 stage 올림

위 과정을 모든 경매 대상에 대해 1회씩 실시

<b> 3. 2차 경매 시작 ~ 경매 종료 </b>

경매 대상 stage 올림 -> 입찰 전 대기시간 -> 입찰 시작 -> 입찰 종료 -> 낙찰/유찰 판단 후 소속팀 결정
-> 다음 경매대상 stage 올림

위 과정을 1차 경매에서 유찰된 대상들에게 1회씩 실시후 경매 종료

### 구현 과정 스크린샷 ###

![image](https://github.com/sungwoon129/TeamMaker/assets/43958570/c5198ac8-f8e0-4998-b50a-0bed352aab6f)
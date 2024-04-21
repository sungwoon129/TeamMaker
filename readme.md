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


### 구현 과정 스크린샷 ###

![image](https://github.com/sungwoon129/TeamMaker/assets/43958570/4f369bb4-6914-4fb8-98ed-7ce0ba156e99)
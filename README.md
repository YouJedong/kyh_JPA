# kyh_JPA
김영한의 JPA 로드맵 

### JPA 소개

- SQL 중점적인 개발의 문제점
    - Mapping 작업이 너무 오래걸린다.
    - 객체를 자바 컬렉션에 저장하듯이 저장할 수 없을까? → JPA
- JPA 소개
    - Java Persistence API
    - 자바 진영의 ORM 기술 표준
        
        *ORM? 객체와 관계형 DB와 매핑을 해주는 것
        
    - JPA는 애플리케이션과 JDBC 사이에서 동작
    - **패러다임 불일치 해결**
    - JPA가 만들어지기 전에 하이버네이트라는 오픈소스 ORM이 있었는데 이것을 본따 JPA를 만들었다.
- JPA를 사용하는 이유
    - SQL 중심적인 개발에서 객체 중심으로 개발
    - 생산성 : CRUD를 만들기 쉬움
    - 유지보수 : 필드 변경시 모든 SQL을 수정해야하는 문제를 해결
    - 패러다임 불일치 해결
        1. 상속
        저장 : 상속 관계로 있는 객체를 저장할 때 여러번 insert 코드를 작성하는 것이 아닌 .persist(object)의 하나의 코드로 알아서 insert 해줌
        조회 : 조인이 필요한 쿼리도 .find(object.class, id) 하나의 코드로 알아서 select 해줌
        2. 연관관계, 객체 그래프 탐색 : 연관 관계에 있는 테이블을 쉽게 저장하고 조회할 수 있음
        3. 신뢰할 수 있는 엔티티, 계층
        4. 동일한 트랜잭션에서 조회한 엔티티는 비교했을 때 같음을 보장함
            
            ```java
            Member member1 = jpa.find(Member.class, memberId);
            Member member2 = jpa.find(Member.class, memberId);
            member1 == member2; // true
            ```
            
    - 성능 최적화 기능
        1. 1차 캐시와 동일성 보장 : 같은 트랜잭션 안에서는 처음에는 sql을 날려서 반환하고 같은 엔티티를 조회했을 때 캐시된 엔티티를 들고옴(성능 향상)
        2. 트랜잭션을 지원하는 쓰기 지연
        INSERT : 트랜잭션을 커밋할 때까지 Insert Sql을 모았다가(bufferWirting) commit을 하는 순간 한번에 sql 실행
        3. 지연 로딩과 즉시 로딩 가능
        지연로딩: 객체가 실제 사용될 때 로딩
        즉시로딩: JOIN SQL로 한번에 연관된 객체까지 미리 조회

### JPA 설정하기

- 프로젝트 환경
    - 자바17
    - 하이버네이트 6
    - h2 2.2.224
    - maven
- persistance.xml 설정하기
    - resource/META-INF/persistance.xml을 생성
    - 설정 중 원래 javax의 패키지 이름을 jakarta로 변경해야함
    - propertie설정 중 hibernate.dialect 설정*

        ```java
        <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
        ```

      데이터 베이스의 종류에 따라 특정 함수가 존재하는데 이런 차이에 따른 종속성을 없애기 위해 위 처럼 설정함
      *h2를 사용하면 H2Dialect, mysql을 사용하면 MySQLDialect


### JPA 사용

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
EntityManager em = emf.createEntityManager();
```

- EntityManagerFactory : 어플리케이션이 실행될 때 한번만 생성해서 계속 사용한다.
- EntityManager: 한 요청에 한번 사용하고 버린다.(쓰레드에서 공유하면 x)
- **JPA를 사용하면서 모든 데이터 변경은 트랜젝션 안에서 실행되어야 한다!!**
- JPQL

    ```java
    List<Member> list = em.createQuery("SELECT m FROM Member AS m" ,Member.class)
                    .getResultList();
    ```

    - 직접 쿼리를 작성해서 조회
    - SQL과 JPQL의 차이점: SQL은 db의 테이블을 대상으로 쿼리 / JPQL은 객체를 대상으로 쿼리

## 영속성 컨텍스트

- JPA를 통해 DB에 데이터를 넣기전에 엔티티의 상태를 관리하는 장소

### 영속성 컨텍스트의 생명주기

1. 비영속(new/transient) - 영속성 컨텍스트와 전혀 상관없는 새로운 상태
    
    ```java
    // 생성만 한 상태
    Member m = new Member();
    m.setId(1);
    m.setName("jedong");
    ```
    
2. 영속(managed) - 영속성 컨텍스트에서 관리되고 있는 상태 → 조회, 등록 시 영속상태가 됨
    
    ```java
    // 영속성 컨텍스트에 담김
    em.persist(m)
    ```
    
3. 준영속(detached) - 영속성 컨텍스트에 저장되었다가 분리된 상태
4. 삭제(remove) - 삭제된 상태

### 영속성 컨텍스트 존재의 이점

1. 1차 캐시
    - 엔티티를 key와 value 형태로 영속성 컨텍스트에 저장을 해놓고 DB에 조회할 때 캐시에 해당 데이터가 있으면 DB에 요청하지 않고 캐시 데이터를 사용한다. → DB 호출 횟수를 줄인다.
    - 하지만 하나의 Transaction에서 사용되는 1차 캐시이기 때문에 큰 이점은 없다.
2. 영속 엔티티의 동일성을 보장
    - 1차 캐시로 저장하기 때문에 여러번 같은 엔티티를 조회하면 동일성을 보장한다.
        
        ```java
        Member m1 = em.find(Member.class, 101);
        Member m2 = em.find(Member.class, 101);
        
        System.out.println(m1 == m2); // true
        ```
        
3. 트랜젝션을 지원하는 쓰기 지연
    - 엔티티를 저장했을 때 바로 insert하지 않고 영속성 컨텍스트에 ‘1차 캐시’안에 넣고 ‘**쓰기SQL 저장소**’에  담아놓았다가 commit이 실행될 때 한번에 insert쿼리를 처리한다.
4. 엔티티 변경 감지(dirty checking)
    - 엔티티를 영속성 컨텍스트에 넣은 후 값을 변경하기만 하면 자바의 컬렉션처럼 그 값이 변경됨
    - 처음 엔티티가 영속성 컨텍스트에 담겼을 때 스냅샷을 찍어놓고 트랜젝션이 commit되는 시점에 변경을 감지해서 update문을 실행한다.

### Flush

- 영속성 컨텍스트의 저장된 데이터를 DB에 반영하는 것
- 영송성 컨텍스트를 flush하는 방법
    1. 직접 flush하기 ex.flush()
    2. 트랜젝션 커밋(자동 반영)
    3. JPQL 쿼리 수행
- flush가 실행될 때 영속성 컨텍스트가 비워지는 것이 아닌 해당 쿼리만 즉시 실행하는 것

## 엔티티 맵핑

### @Entity

- 이 어노테이션이 붙어야만 JPA에서 관리한다.
- **기본 생성자 필수**(protect, public으로 만들어야함)

### 기타 정보

- @Column 어노테이션의 속성은 ddl생성시에만 이용하고 런타임 중에는 영향을 주지 않는다.(속성으로 입력한 코드들은 ddl 생성할 때만 영향이 있다!)
## 필드와 컬럼 맵핑

### Entity 셋팅

1. **@Enumerated**
    - 자바의 Enum을 사용하고 싶을 때 Entity의 컬럼에 @Enumerated를 사용한다.
        
        ```java
        @Enumerated(EnumType.STRING)
        private RoleType roleType;
        ```
        
    - *JPA > Enum 활용법
        
        [Java Enum 활용기 | 우아한형제들 기술블로그](https://techblog.woowahan.com/2527/)
        
2. **@Temporal**
    - 일시 컬럼은 @Temporal을 사용한다.
        
        ```java
        @Temporal(TemporalType.TIMESTAMP)
        private Date createdDate;
        ```
        
    - 최신 하이버네이트에서는 이 어노테이션을 사용하지 않고 **LocalDate**, **LocalDateTime** 클래스를 사용해도 된다.
3. **@Lob**
    - varchar를 넘어서는 컨텐츠는 @Lob을 사용한다.
        
        ```java
        @Lob
        private String description;
        ```
        
4. @Transient
    - DB와 맵핑시키지 않고 자바에서만 사용하고 싶을 때

### @Column의 속성

1. insertable, updatable
    - 등록, 변경의 가능 여부
    - 기본은 true - 등록, 변경 시 해당 컬럼 변경됨
    - false로 셋팅하면 데이터 등록, 변경 시 db에 반영 안됨
    

### @Enumerated 주의점

- 속성에 ordinary를 사용하지 마라!
    - ordinary를 사용하게 되면 db에 enum의 값이 순서대로 숫자로 들어가기 때문에 나중에 enum에 값을 추가하면 어떤 값인지 알 수 없음
- 기본값이 ordinary이기 때문에 반드시 속성에 String으로 셋팅하자
    
    ```java
    @Enumerated(EnumType.STRING)
    private RoleType roleType;
    ```
    

### @GeneratedValue

**GenerationType.*IDENTITY의 특징***

- 이 속성은 db의 기본키 전략을 이용하기 때문에 JPA로 insert할 때 영속성 컨텍스트에 보관하지 않고 바로 db로 쿼리를 날린다.
    
    why? 직접 db에 날리지 않으면 현재 insert한 레코드의 id를 알 수 가 없다. 그래서  insert를 한 후 해당 id를 알기위해 바로 db에 날리는 것
    

**GenerationType.SEQUENCE 사용 시 최적화**

- 이 속성은 미리 DB에 시퀀스를 만들어 사용하는 속성이다.
- insert를 할 때 지금 사용해야할 id의 시퀀스를 가져오기 때문에 insert할 때마다 db 접속을 한다.
- 이런 성능 저하를 최적화 하기위해 **미리 특정 개수의 시퀀스를 미리 가지고 온다**.
    
    ```java
    @Entity
    @Table(name = "Member")
    @SequenceGenerator(
            name = "MEMBER_SEQ_GENERATOR",
            sequenceName = "MEMBER_SEQ",
            initialValue = 1, allocationSize = 50 // 1부터 시작해서 +50으로 늘린다. >> 이곳에서 50까지 미리 할당해 놓는다.
    )
    public class Member0505 {
    
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "MEMBER_SEQ_GENERATOR")
        private Long id;
    ```
    

*H2 연결 오류 해결

- 오류 : h2에서 새로운 db 생성이 안됨
- 해결 : 새로 생성할 때는 url을 jdbc:h2:~/jpashop 이런식으로 하고 이제 두번째부터는 jdbc:h2:tcp://[localhost/~/jpashop](http://localhost/~/jpashop) 이렇게 들어가야한다.

## 연관관계 매핑

### 객체를 테이블에 맞춰서 맵핑 했을 때 문제점

- 객체(Entity)를 테이블에 맞춰서 코드를 작성 했을 때!
    - 등록시 - 외래키 식별자를 직접 다룸 → 코드를 작성할 때 외래키가 있다면 직접 해당 외래키를 가지고 등록한다.
    - 조회시 - 외래키를 가진 객체 조회 후 다시 외래키로 한번 더 조회한다.
    - 둘 다 객체지향적인 방법이 아님

### 단방향 연관관계

(jpa-ex-0505 프로젝트 참고)

- 자바 코드에 Entity에 참조할 Entity를 작성하고 @ManyToOne (다 에서 1) 어노테이션을 붙인다.
    
    ```java
    @Entity
    public Member {
    	
    	@Id
    	private Long id;
    	
    	@ManyToOne
    	@JoinColumn(name = "TEAM_ID") // join할 때 필요한 컬럼 셋팅
    	private Team team;
    	
    }
    ```
    

### 양방향 연관관계와 연관관계 주인

(jpa-ex-0505 프로젝트 참고)

**양방향 연관관계?** 

- 테이블에서는 Member와 Team이 TEAM_ID라는 외래키로 양방향으로 연결되어있다. (TEAM_ID만 있으면 서로 연관을 맺을 수 있다.)
- 하지만 자바의 객체에서는 이렇게 양방향으로 연관을 맺으려면 서로 참조하는 필드를 만들어 줘야한다.
    
    ```java
    // Member.class
    @Entity
    public Member {
    		
    		......
    		
    		@ManyToOne
    		private Team team;
    		
    }
    
    // Team.class
    @Entity
    public Team {
    		.....
    		
    		@OneToMany(mappedBy = "team") // 이렇게 양방향으로 맵핑 셋팅
    		private List<Member> members = new ArrayList<>();
    		
    }
    ```
    
- 하지만 사실 객체에서 양방향을 설정했을 때 단방향으로 연결된 필드가 2개가 생기는 것이다.
그렇다면 JPA로 수정,등록을 할 때 Member의 team / Team의 Members 이 둘중 어떤 것을 참조해서 쿼리를 날려야 할까?
(Member의 team에 값을 변경해도 DB가 수정되고 Team의 Members의 값을 수정해도 DB에서 수정되는 오류가 발생함)
- 따라서 둘 중 한군데에서만 등록/수정을 처리해야한다. → **연관관계의 주인**을 설정해야한다.

**연관관계의 주인**

- 연관 관계의 주인을 설정하려면 주인이 아닌 필드에 mappedBy 속성을 설정해야한다.
    
    ```java
    @OneToMany(mappedBy = "team") // "Member의 team이라는 변수가 주인이다" 라는 설정
    private List<Member> members = new ArrayList<>();
    ```
    
- 이렇게 설정하면 연관관계의 주인인 Member의 team은 **등록/수정/조회**가 가능하고 Team의 members는 **조회**만 가능하다
- **누구를 주인으로 설정해야할까? → 외래키가 있는 객체를 주인으로 정하라**

**양방향 연관관계 사용시 주의점**

- 실제로 등록/수정되는 필드에 값을 넣어야 JPA에서 해당 쿼리가 동작한다. → 하지만 객체 지향적으로 사용하기 위해 mappedBy로 설정한 필드에도 값을 설정해 놓는다.
    
    ```java
    // Main.class
    Team t = new Team();
    t.setName("team1");
    em.persist(t);
    
    Member m = new Member();
    m.setName("member1");
    m.setTeam(t); // 실제 Member에 teamId가 맵핑되는 코드
    em.persist(m);
    
    t.getMembers().add(m); // 등록되는 쿼리가 실행되지는 않지만 객체 지향적으로 셋팅해주는 곳
    ```
    
    *t.getMembers().add(m); 코드의 이점
    
    - JPA의 구조 상 바로 insert되지않고 영속성 컨텍스트에 저장해놓기 때문에 위의 로직이 실행된 후 t.getMembers()의 값을 사용해야 한다면 반드시 add를 해놓아야한다.

**양뱡향 연관관계 사용 결론**

1. t.getMembers().add(m); 이런식으로 셋팅하는 것을 누락할 수 있기 때문에 **연관관계 편의 메서드**를 만들자
    
    ```java
    //Member.class
    public Member {
    		....
    		
    		public changeTeam(Team team) {
    				this.team = team;
    				
    				team.getMembers().add(this); // 이렇게 member(=this)를 넣는 코드를 추가한다. 
    		}		
    
    }
    ```
    
2. 양방향 매핑시 무한 루프를 조심해라! → toString, lombok의 toString, Json 생성라이브러리
    - lombok의 toString()은 쓰지마라!
    - Json 생성라이브러리 ex) Spring의 controller단 response → 응답을 Entity로 하지말고 Dto를 만들어서 반환해라
3. 단방향 매핑만으로도 이미 연관관계 매핑은 완료된 것이고 양방향 매핑은 객체 그래프 탐색 기능을 추가한 것 뿐이다.
따라서 단방향 매핑만 잘하고 그 후 개발하면서 필요할 때 양방향 매핑을 사용하라

> 연관관계 매핑 실전 예제 : jpa-ex-05062
>

### 일대다 [1:N]

- 보통 ‘다’가 주인인 연관관계를 맺는데 일대다 연관관계가 있을 수 있음
- 보통 다대일 연관관계를 많이 씀
- 다대일 연관관계보다 쿼리가 더 많이 나가는 단점이 있음(성능이슈) - 연관관계 관리를 위해 추가로 UPDATE SQL이 실행됨
- 실무에서는 유지보수를 위해 일대다 보다는 **다대일 양방향**을 쓰자

> 일대다 연관관계 매핑 예제 : jpa-ex-0508-member-team
>

## 일대일

> 일대일 예제 : jpa-ex-0511-member-locker
> 

### 일대일 매핑 방법

```java
// Member.class
@Entity
Member {
	.........
	
	@OneToOne
	@JoinColumn(name = "LOCKER_ID")
	private String Locker
}

// Locker.class
@Entity
Locker {
	.........
	
	@OneToOne(mappedBy = "locker")
	private String Locker
}
```

1. ManyToOne과 같이 외래키가 있는 엔티티에 @OneToOne과 @JoinColumn을 셋팅한다.
2. 양방향으로 쓰고싶다면 매칭된 엔티티에 @OneToOne과 속성으로 mappedBy를 추가한다.

### 일대일 연관관계일 때 DB에서 외래키의 위치

- 일대일 연관관계라면 외래키의 위치를 Member, Locker 테이블 둘 중 하나로 설정할 수 있음
    - ex) member_id를 Locker테이블의 외래키+유니크로 `or` locker_id를 Member테이블의 외래키+유니크 → 둘중 하나로 선택 가능
- 두 선택지는 장단점을 가지고 있음
    - Member(주 테이블)에 외래키를 놓을 경우
        - 장점: 주 테이블만 조회해도 대상 테이블(Locker)에 데이터가 있는지 확인 가능
        - 단점: 값이 없으면 외래키에 null이 허용됨
    - Locker(대상 테이블)에 외래키를 놓을 경우
        - 장점: 나중에 일대일관계에서 일대다로 바뀌었을 때 DB수정이 용이함
        - 단점: JPA의 지연 로딩을 사용할 수 없어서 즉시 로딩됨
- **결론: JPA를 사용할 때는 주테이블(Member)에 외래키를 놓는 것이 적절함 하지만 DB 측면에서는 애매할 수 있기 때문에 DBA와 적절한 대화가 필요**

## 다대다

- 결론적으로 사용하면 안됨
- @ManyToMany로 두 객체를 연결하면 자동으로 DB에 중간 테이블이 생성되지만 그 중간 테이블에 다른 컬럼을 설정할 수 없음
- **결론 : @ManyToMany를 사용하지 말고 실제로 중간 객체를 만들어서 일대다 다대일 관계로 만들어서 사용하라**
    
    <img width="578" alt="노션이미지01" src="https://github.com/YouJedong/kyh_JPA/assets/108327853/034d50fd-bb32-45d8-97a5-3e424218b9f9">
    
    - 중간 테이블에서 ORDER_ID로 PK를 둘 수도 있고 MEMBER_ID + PRODUCT_ID를 합쳐서 PK로 둘 수 있는데 JPA에서는 따로 ORDER_ID를 만들어서 사용하는 것을 **추천**

## 상속관계 맵핑

- 객체는 상속의 기능이 있지만 DB는 상속의 기능이 없다.
- 따라서 DB에서는 객체의 상속의 기능을 3가지 방식으로 만들 수 있다.

### 1. 조인 전략

<img width="372" alt="노션이미지02" src="https://github.com/YouJedong/kyh_JPA/assets/108327853/925a22a9-b43a-4409-8506-6b86961abbe3">

- 상위 테이블 1개와 하위 테이블 3개를 만드는 전략
- 부모 클래스에 @Inheritance로 설정한다.
    
    ```java
    // Item.class
    @Entity
    @Inheritance(strategy = InheritanceType.JOINED)
    abstract class Item {
    	.....
    }
    ```
    
    - 그럼 자식 클래스를 등록이나 조회를 했을 때 상위 테이블도 같이 insert, join한다.
- 하위 테이블의 값을 상위 테이블에 저장하고 싶다면 (DTYPE) @DiscriminatorColumn을 사용하면 컬럼이 자동 생성되고
자식 클래스의 이름으로 값이 들어간다.
    
    ```java
    @Entity
    @Inheritance(strategy = InheritanceType.JOINED)
    @DiscriminatorColumn
    public abstract class Item {}
    ```
    

### 2. 단일 테이블 전략

- 테이블 하나만 만들어서 모든 컬럼을 다 넣고 타입값으로 무슨 종류인지 구분하는 전략
- @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
- @DiscriminatorColumn이 없어도 dtype이 자동생성된다.

### 3. 구현 클래스마다 생성

- 테이블을 완전 나누는 전략 @Inheritance(strategy = InheritanceType.*TABLE_PER_CLASS*)
- @DiscriminatorColumn이 필요없다.
- 단점 - 부모 클래스로 조회할 때 따로 만들어진 테이블을 다 union all해서 가져온다.
    
    ```java
    Item findItem = em.find(Item.class, movie.getId());
    ```
    

### 전략 장단점

| 전략 | 장점 | 단점 |
| --- | --- | --- |
| 조인 | 테이블 정규화, 저장공간 효율화 | 조회 시 조인이 많아질 수 있음, 저장할 때 insert 2번 나감, 복잡함 |
| 단일 테이블 | 조인이 필요x, 쿼리 단순 | 모든 자식 컬럼은 null 허용해야함, 테이블이 커져서 성능이 느려질 수 있음 |
| 구현 클래스 |  | 쓰지 말자^^ |

**결론: 조인 전략과 단일 테이블 전략의 트레이드 오프를 비교해서 둘 중에 하나로 결정하자**

### @Mapped Superclass

- 공통으로 쓰는 컬럼을 설정하는 클래스에 어노테이션을 붙이고 하위 클래스에서 상속을 받으면 DB에 각각 테이블에 컬럼이 생성됨
## 상속 및 MappedSuperclass 예제

> 예제 - jpa-ex-0513_1-jpaShop
> 

### 예제 진행 중 오류 해결

```java
// 오류
@ManyToMany(mappedBy = "items")
private ArrayList<Category> categories = new ArrayList<>();

// 해결법
private List<Category> categories = new ArrayList<>();
```

- 이전 예제 진행 중 위의 코드처럼 Entity안에 해당 필드를 ArrayList로 했더니 타입 오류가 나왔다.
- ArrayList를 List로 바꾸었더니 해결되었다.
- 이유 : ??
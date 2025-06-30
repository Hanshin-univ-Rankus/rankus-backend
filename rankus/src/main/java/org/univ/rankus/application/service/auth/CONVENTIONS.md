# Auth Service 컨벤션

## 클래스 네이밍
- 서비스 클래스: `AuthService`
- 메서드: `{action}()` - `signup()`, `login()`, `logout()`

## 표준 구조
```java
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public User signup(UserRegisterRequestDto request) {
        // 구현: User Entity 반환
    }
    
    public AuthResponseDto login(UserLoginRequestDto request) {
        // 구현: 특별히 인증 정보 DTO 반환 (토큰 + 사용자 정보)
    }
}
```

## 메서드 패턴
- 회원가입: `signup(UserRegisterRequestDto) -> User` (Entity 반환)
- 로그인: `login(UserLoginRequestDto) -> AuthResponseDto` (인증 정보 DTO 반환)
- 토큰 갱신: `refreshToken(String) -> AuthResponseDto`

## 반환값 규칙
- **회원가입**: User Entity 반환 (일반적인 Service 패턴)
- **로그인**: AuthResponseDto 반환 (토큰 + 사용자 정보 복합 객체)
- **특수 케이스**: 인증 서비스는 토큰 생성이 포함되어 DTO 반환이 허용됨

## 검증 로직
1. 이메일 중복 검사 (회원가입)
2. 비밀번호 검증 (로그인)
3. 토큰 유효성 검사

## 예외 처리
- `UserValidationException` - 입력값 검증 실패
- `DuplicateEmailException` - 이메일 중복
- `InvalidCredentialsException` - 로그인 실패

## 트랜잭션
- `@Transactional` - 회원가입 시 사용자 생성
- `@Transactional(readOnly = true)` - 로그인 검증
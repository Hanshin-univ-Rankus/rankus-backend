package org.univ.rankus.application.service.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ.rankus.application.port.out.EmailSendPort;
import org.univ.rankus.application.port.out.EmailVerificationRepositoryPort;
import org.univ.rankus.application.port.out.UserRepositoryPort;
import org.univ.rankus.domain.model.user.EmailVerification;
import org.univ.rankus.domain.model.user.User;
import org.univ.rankus.domain.model.user.UserStatus;
import org.univ.rankus.testutil.factory.domain.DomainUserFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationRepositoryPort emailVerificationRepository;

    @Mock
    private EmailSendPort emailSendPort;

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Nested
    @DisplayName("verifyCode 메서드는")
    class VerifyCodeTests {

        @Test
        @DisplayName("인증 코드 검증 성공 시, 사용자 상태를 ACTIVE로 변경한다")
        void verifyCode_Success_ActivatesUser() {
            // given
            String email = "test@hs.ac.kr";
            EmailVerification verification = new EmailVerification(email);
            String realCode = verification.getVerificationCode(); // 실제 생성된 코드를 가져옴
            User pendingUser = DomainUserFactory.buildUserWithStatus(UserStatus.PENDING);

            when(emailVerificationRepository.findLatestByEmail(email)).thenReturn(Optional.of(verification));
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(pendingUser));
            when(emailVerificationRepository.save(any(EmailVerification.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            emailVerificationService.verifyCode(email, realCode);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }
}

package com.abidria.data.auth

import com.abidria.data.common.Result
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mockito.mock

class AuthRepositoryTest {

    @Test
    fun test_has_person_credentials_returns_true() {
        given {
            an_auth_storage_repository_that_returns_an_auth_token()
        } whenn {
            has_person_credentials()
        } then {
            result_is_true()
        }
    }

    @Test
    fun test_has_not_person_credentials_returns_false() {
        given {
            an_auth_storage_repository_that_raises_no_logged_exception()
        } whenn {
            has_person_credentials()
        } then {
            result_is_false()
        }
    }

    @Test
    fun test_can_create_content_returns_false_if_no_person_info() {
        given {
            an_auth_api_that_raises_no_person_info_on_get_person_info()
        } whenn {
            can_create_content()
        } then {
            should_return_false()
        }
    }

    @Test
    fun test_can_create_content_returns_false_if_person_is_email_confirmed_false() {
        given {
            a_person_with_is_email_confirmed_false()
            an_auth_api_that_returns_that_person_on_get_person()
        } whenn {
            can_create_content()
        } then {
            should_return_false()
        }
    }

    @Test
    fun test_can_create_content_returns_true_if_person_is_email_confirmed_true() {
        given {
            a_person_with_is_email_confirmed_true()
            an_auth_api_that_returns_that_person_on_get_person()
        } whenn {
            can_create_content()
        } then {
            should_return_true()
        }
    }

    @Test
    fun test_get_person_invitation_returns_flowable_and_saves_auth_token() {
        given {
            an_auth_api_that_returns_a_flowable_with_auth_token()
        } whenn {
            get_person_invitation()
        } then {
            should_return_auth_token_flowable()
            should_save_auth_token_on_auth_storage_repository()
        }
    }

    @Test
    fun test_save_person_call_storage_repo_set_person() {
        given {
            a_person_with_is_email_confirmed_true()
        } whenn {
            save_that_person()
        } then {
            should_call_storage_repo_set_person_with_that_person()
        }
    }

    @Test
    fun test_register_returns_api_register_return_and_saves_person_on_storage_repo() {
        given {
            a_username()
            an_email()
            a_person()
            an_auth_api_that_returns_a_flowable_with_that_person()
        } whenn {
            register_person()
        } then {
            should_call_api_register_with_username_and_email()
            should_receive_person()
            should_call_storage_repo_to_save_person()
        }
    }

    @Test
    fun test_register_returns_error_without_trying_to_save_it() {
        given {
            a_username()
            an_email()
            a_result_error()
            an_auth_api_that_returns_a_flowable_with_that_result_error()
        } whenn {
            register_person()
        } then {
            should_call_api_register_with_username_and_email()
            should_receive_error_result()
            should_not_call_storage_repo_to_save_person()
        }
    }

    @Test
    fun test_confirm_email_returns_api_confirm_email_return_and_saves_person_on_storage_repo() {
        given {
            a_confirmation_token()
            a_person()
            an_auth_api_that_returns_a_flowable_with_that_person_on_confirm_email()
        } whenn {
            confirm_email_person()
        } then {
            should_call_api_confirm_email_with_confirmation_token()
            should_receive_person()
            should_call_storage_repo_to_save_person()
        }
    }

    @Test
    fun test_confirm_email_returns_error_without_trying_to_save_it() {
        given {
            a_confirmation_token()
            a_result_error()
            an_auth_api_that_returns_a_flowable_with_that_result_error_on_confirm_email()
        } whenn {
            confirm_email_person()
        } then {
            should_call_api_confirm_email_with_confirmation_token()
            should_receive_error_result()
            should_not_call_storage_repo_to_save_person()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        val authStorageRepository = mock(AuthStorageRepository::class.java)
        val authApiRepository = mock(AuthApiRepository::class.java)
        val repository = AuthRepository(authStorageRepository, authApiRepository)
        var hasCredentialsResult = false
        var canCreateContentResult = false
        val testAuthTokenSubscriber = TestSubscriber<Result<AuthToken>>()
        val testPersonSubscriber = TestSubscriber<Result<Person>>()
        lateinit var authToken: AuthToken
        lateinit var person: Person
        var username = ""
        var email = ""
        var confirmationToken = ""
        var resultError: Result<Person>? = null

        fun a_username() {
            username = "usr.nm"
        }

        fun an_email() {
            email = "e@m.c"
        }

        fun a_confirmation_token() {
            confirmationToken = "ASDF"
        }

        fun a_person() {
            person = Person(isRegistered = true, username = "srnm", email = "test@m.c", isEmailConfirmed = false)
        }

        fun a_result_error() {
            resultError = Result(null, ClientException(source = "s", code = "c", message = "m"))
        }

        fun an_auth_api_that_returns_a_flowable_with_that_person() {
            BDDMockito.given(authApiRepository.register(username, email))
                    .willReturn(Flowable.just(Result(person, null)))
        }

        fun an_auth_api_that_returns_a_flowable_with_that_result_error() {
            BDDMockito.given(authApiRepository.register(username, email)).willReturn(Flowable.just(resultError))
        }

        fun an_auth_api_that_returns_a_flowable_with_that_person_on_confirm_email() {
            BDDMockito.given(authApiRepository.confirmEmail(confirmationToken))
                    .willReturn(Flowable.just(Result(person, null)))
        }

        fun an_auth_api_that_returns_a_flowable_with_that_result_error_on_confirm_email() {
            BDDMockito.given(authApiRepository.confirmEmail(confirmationToken)).willReturn(Flowable.just(resultError))
        }

        fun an_auth_storage_repository_that_returns_an_auth_token() {
            val authToken = AuthToken(accessToken = "A", refreshToken = "R")
            BDDMockito.given(authStorageRepository.getPersonCredentials()).willReturn(authToken)
        }

        fun an_auth_storage_repository_that_raises_no_logged_exception() {
            BDDMockito.given(authStorageRepository.getPersonCredentials()).willThrow(NoLoggedException("Error"))
        }

        fun an_auth_api_that_returns_a_flowable_with_auth_token() {
            authToken = AuthToken("A", "R")
            BDDMockito.given(authApiRepository.getPersonInvitation()).willReturn(
                    Flowable.just(Result(authToken, null)))
        }

        fun has_person_credentials() {
            hasCredentialsResult = repository.hasPersonCredentials()
        }

        fun get_person_invitation() {
            repository.getPersonInvitation().subscribeOn(Schedulers.trampoline()).subscribe(testAuthTokenSubscriber)
        }

        fun register_person() {
            repository.register(username = username, email = email)
                    .subscribeOn(Schedulers.trampoline()).subscribe(testPersonSubscriber)
            testPersonSubscriber.awaitCount(1)
        }

        fun confirm_email_person() {
            repository.confirmEmail(confirmationToken = confirmationToken)
                    .subscribeOn(Schedulers.trampoline()).subscribe(testPersonSubscriber)
            testPersonSubscriber.awaitCount(1)
        }

        fun result_is_true() {
            assertTrue(hasCredentialsResult)
        }

        fun result_is_false() {
            assertFalse(hasCredentialsResult)
        }

        fun should_return_auth_token_flowable() {
            testAuthTokenSubscriber.assertResult(Result(authToken, null))
        }

        fun should_save_auth_token_on_auth_storage_repository() {
            BDDMockito.then(authStorageRepository).should().setPersonCredentials(authToken)
        }

        fun an_auth_api_that_raises_no_person_info_on_get_person_info() {
            BDDMockito.given(authStorageRepository.getPerson())
                    .willThrow(NoPersonInfoException("Person has not started register process"))
        }

        fun a_person_with_is_email_confirmed_false() {
            person = Person(isRegistered = true, username = "usr", email = "e@m.c", isEmailConfirmed = false)
        }

        fun a_person_with_is_email_confirmed_true() {
            person = Person(isRegistered = true, username = "usr", email = "e@m.c", isEmailConfirmed = true)
        }

        fun an_auth_api_that_returns_that_person_on_get_person() {
            BDDMockito.given(authStorageRepository.getPerson()).willReturn(person)
        }

        fun can_create_content() {
            canCreateContentResult = repository.canPersonCreateContent()
        }

        fun save_that_person() {
            repository.savePerson(person)
        }

        fun should_return_false() {
            assertFalse(canCreateContentResult)
        }

        fun should_return_true() {
            assertTrue(canCreateContentResult)
        }

        fun should_call_storage_repo_set_person_with_that_person() {
            BDDMockito.then(authStorageRepository).should().setPerson(person)
        }

        fun should_call_api_register_with_username_and_email() {
            BDDMockito.then(authApiRepository).should().register(username = username, email = email)
        }

        fun should_receive_person() {
            testPersonSubscriber.assertResult(Result(person, null))
        }

        fun should_call_storage_repo_to_save_person() {
            BDDMockito.then(authStorageRepository).should().setPerson(person)
        }

        fun should_receive_error_result() {
            testPersonSubscriber.assertResult(resultError)
        }

        fun should_not_call_storage_repo_to_save_person() {
            BDDMockito.then(authStorageRepository).shouldHaveZeroInteractions()
        }

        fun should_call_api_confirm_email_with_confirmation_token() {
            BDDMockito.then(authApiRepository).should().confirmEmail(confirmationToken)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
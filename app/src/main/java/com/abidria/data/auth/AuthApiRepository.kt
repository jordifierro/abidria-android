package com.abidria.data.auth

import com.abidria.data.common.NetworkParserFactory
import com.abidria.data.common.Result
import io.reactivex.Flowable
import retrofit2.Retrofit

class AuthApiRepository (retrofit: Retrofit, val clientSecretKey: String) {

    private val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    fun getPersonInvitation(): Flowable<Result<AuthToken>> {
        return authApi.createPersonResource(clientSecretKey = clientSecretKey)
                .compose(NetworkParserFactory.getTransformer())
    }

    fun register(username: String, email: String): Flowable<Result<Person>> {
        return authApi.register(username = username, email = email)
                .compose(NetworkParserFactory.getErrorTransformer({ ClientExceptionMapper(it).toError() }))
    }

    fun confirmEmail(confirmationToken: String): Flowable<Result<Person>> {
        return authApi.confirmEmail(confirmationToken = confirmationToken)
                .compose(NetworkParserFactory.getErrorTransformer({ ClientExceptionMapper(it).toError() }))
    }
}

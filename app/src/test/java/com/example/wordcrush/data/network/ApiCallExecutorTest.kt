package com.example.wordcrush.data.network

import com.example.wordcrush.data.model.ApiResponse
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import retrofit2.Response

class ApiCallExecutorTest {
    private val executor = ApiCallExecutor(Gson())

    @Test
    fun executeReturnsDataFromStandardResponse() = runBlocking {
        val payload = executor.execute {
            Response.success(ApiResponse(code = 200, msg = "success", data = "ok"))
        }

        assertEquals("ok", payload.data)
        assertEquals("success", payload.message)
    }

    @Test
    fun executeMapsBusinessFailure() = runBlocking {
        try {
            executor.execute {
                Response.success(ApiResponse<String>(code = 400, msg = "invalid request"))
            }
            throw AssertionError("Expected NetworkException.Business")
        } catch (error: NetworkException.Business) {
            assertEquals(400, error.code)
            assertEquals("invalid request", error.detail)
        }
    }

    @Test
    fun executeMapsUnauthorizedHttpFailure() = runBlocking {
        try {
            executor.execute {
                Response.error<ApiResponse<String>>(
                    401,
                    "{\"code\":401,\"msg\":\"invalid token\"}"
                        .toResponseBody("application/json".toMediaType())
                )
            }
            throw AssertionError("Expected NetworkException.Unauthorized")
        } catch (error: NetworkException.Unauthorized) {
            assertSame(NetworkException.Unauthorized, error)
        }
    }
}

package com.example.wordcrush.data.network

import com.example.wordcrush.data.model.ApiResponse
import com.wordcrush.api.ApiCode
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class ApiCallExecutorTest {
    private val executor = ApiCallExecutor(Gson())

    @Test
    fun executeReturnsDataFromStandardResponse() = runBlocking {
        val payload = executor.execute {
            Response.success(ApiResponse(code = ApiCode.SUCCESS.value(), msg = "success", data = "ok"))
        }

        assertEquals(ApiCode.SUCCESS.value(), payload.code)
        assertEquals("ok", payload.data)
        assertEquals("success", payload.message)
    }

    @Test
    fun executeMapsBusinessEnvelopeFailure() = runBlocking {
        try {
            executor.execute {
                Response.success(ApiResponse<String>(code = ApiCode.BAD_REQUEST.value(), msg = "invalid request"))
            }
            throw AssertionError("Expected NetworkException.Server")
        } catch (error: NetworkException.Server) {
            assertEquals(ApiCode.SUCCESS.value(), error.httpStatusCode)
            assertEquals(ApiCode.BAD_REQUEST.value(), error.code)
            assertEquals("invalid request", error.detail)
        }
    }

    @Test
    fun executeMapsUnauthorizedHttpFailure() = runBlocking {
        try {
            executor.execute {
                Response.error<ApiResponse<String>>(
                    ApiCode.UNAUTHORIZED.value(),
                    """{"code":${ApiCode.UNAUTHORIZED.value()},"msg":"invalid token"}"""
                        .toResponseBody("application/json".toMediaType())
                )
            }
            throw AssertionError("Expected NetworkException.Server")
        } catch (error: NetworkException.Server) {
            assertEquals(ApiCode.UNAUTHORIZED.value(), error.httpStatusCode)
            assertEquals(ApiCode.UNAUTHORIZED.value(), error.code)
            assertEquals("invalid token", error.detail)
        }
    }

    @Test
    fun executeMapsEmptySuccessfulDataWithoutRequiringData() = runBlocking {
        val payload = executor.execute {
            Response.success(ApiResponse<Unit>(code = ApiCode.SUCCESS.value(), msg = "success", data = null))
        }

        assertEquals(ApiCode.SUCCESS.value(), payload.code)
        assertEquals("success", payload.message)
        assertEquals(null, payload.data)
    }

    @Test
    fun executeUsesHttpStatusWhenErrorBodyIsNotStandardJson() = runBlocking {
        try {
            executor.execute {
                Response.error<ApiResponse<String>>(
                    ApiCode.SERVICE_UNAVAILABLE.value(),
                    "upstream unavailable".toResponseBody("text/plain".toMediaType())
                )
            }
            throw AssertionError("Expected NetworkException.Server")
        } catch (error: NetworkException.Server) {
            assertEquals(ApiCode.SERVICE_UNAVAILABLE.value(), error.httpStatusCode)
            assertEquals(ApiCode.SERVICE_UNAVAILABLE.value(), error.code)
            assertEquals("upstream unavailable", error.detail)
        }
    }
}

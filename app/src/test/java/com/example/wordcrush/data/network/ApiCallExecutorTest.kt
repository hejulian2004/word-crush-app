package com.example.wordcrush.data.network

import com.example.wordcrush.data.model.ApiResponse
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
            Response.success(ApiResponse(code = 200, msg = "success", data = "ok"))
        }

        assertEquals(200, payload.code)
        assertEquals("ok", payload.data)
        assertEquals("success", payload.message)
    }

    @Test
    fun executeMapsBusinessEnvelopeFailure() = runBlocking {
        try {
            executor.execute {
                Response.success(ApiResponse<String>(code = 400, msg = "invalid request"))
            }
            throw AssertionError("Expected NetworkException.Server")
        } catch (error: NetworkException.Server) {
            assertEquals(200, error.httpStatusCode)
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
            throw AssertionError("Expected NetworkException.Server")
        } catch (error: NetworkException.Server) {
            assertEquals(401, error.httpStatusCode)
            assertEquals(401, error.code)
            assertEquals("invalid token", error.detail)
        }
    }

    @Test
    fun executeMapsEmptySuccessfulDataWithoutRequiringData() = runBlocking {
        val payload = executor.execute {
            Response.success(ApiResponse<Unit>(code = 200, msg = "success", data = null))
        }

        assertEquals(200, payload.code)
        assertEquals("success", payload.message)
        assertEquals(null, payload.data)
    }

    @Test
    fun executeUsesHttpStatusWhenErrorBodyIsNotStandardJson() = runBlocking {
        try {
            executor.execute {
                Response.error<ApiResponse<String>>(
                    503,
                    "upstream unavailable".toResponseBody("text/plain".toMediaType())
                )
            }
            throw AssertionError("Expected NetworkException.Server")
        } catch (error: NetworkException.Server) {
            assertEquals(503, error.httpStatusCode)
            assertEquals(503, error.code)
            assertEquals("upstream unavailable", error.detail)
        }
    }
}

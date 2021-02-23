// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.sps.data.UserVerifier;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TokenValidatorTest {

  private static final UserVerifier MOCK_USER_VERIFIER = mock(UserVerifier.class);
  private static final String ID_TOKEN = "abcde";
  private static final String USER_ID = "12345";
  private static HttpServletRequest MOCK_REQUEST;
  private static HttpServletResponse MOCK_RESPONSE;
  private StringWriter responseStringWriter;
  private PrintWriter responsePrintWriter;

  @Before
  public void setUp() throws Exception {
    MOCK_REQUEST = mock(HttpServletRequest.class);
    MOCK_RESPONSE = mock(HttpServletResponse.class);
    responseStringWriter = new StringWriter();
    responsePrintWriter = new PrintWriter(responseStringWriter);
    when(MOCK_RESPONSE.getWriter()).thenReturn(responsePrintWriter);
  }

  @Test
  public void validateAndGetId_valid_getUserId() throws Exception {
    when(MOCK_REQUEST.getParameter("idToken")).thenReturn(ID_TOKEN);
    when(MOCK_USER_VERIFIER.getUserIdByToken(ID_TOKEN)).thenReturn(Optional.of(USER_ID));

    assertEquals(Optional.of(USER_ID),
        TokenValidator.validateAndGetId(
            MOCK_REQUEST, MOCK_RESPONSE, MOCK_USER_VERIFIER, "test", true));
  }

  @Test
  public void validateAndGetId_nullToken_sendError() throws Exception {
    when(MOCK_REQUEST.getParameter("idToken")).thenReturn(null);

    assertEquals(Optional.empty(),
        TokenValidator.validateAndGetId(
            MOCK_REQUEST, MOCK_RESPONSE, MOCK_USER_VERIFIER, "test", true));
    verify(MOCK_RESPONSE).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), any(String.class));
  }

  @Test
  public void validateAndGetId_emptyToken_sendError() throws Exception {
    when(MOCK_REQUEST.getParameter("idToken")).thenReturn("");

    assertEquals(Optional.empty(),
        TokenValidator.validateAndGetId(
            MOCK_REQUEST, MOCK_RESPONSE, MOCK_USER_VERIFIER, "test", true));
    verify(MOCK_RESPONSE).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), any(String.class));
  }

  @Test
  public void validateAndGetId_invalidToken_sendError() throws Exception {
    when(MOCK_REQUEST.getParameter("idToken")).thenReturn(ID_TOKEN);
    when(MOCK_USER_VERIFIER.getUserIdByToken(ID_TOKEN)).thenReturn(Optional.empty());

    assertEquals(Optional.empty(),
        TokenValidator.validateAndGetId(
            MOCK_REQUEST, MOCK_RESPONSE, MOCK_USER_VERIFIER, "test", true));
    verify(MOCK_RESPONSE).sendError(eq(HttpServletResponse.SC_NOT_FOUND), any(String.class));
  }
}

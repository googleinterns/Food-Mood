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
// limitations under the License.import java.io.IOException;

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import com.google.sps.data.DataAccessor;
import com.google.sps.data.UserVerifier;
import com.google.common.annotations.VisibleForTesting;

 /**
 * A servlet that registering a user, according to a given token.
*/
@WebServlet("/register")
public final class RegistrationServlet extends HttpServlet {

 private static final long serialVersionUID = 1L;
 private UserVerifier userVerifier;
 private DataAccessor dataAccessor;

 @Override
 public void init() {
  this.userVerifier = UserVerifier.create("CLIENT_ID");
  this.dataAccessor = new DataAccessor();
 }

 @VisibleForTesting
 void init(UserVerifier userVerifier, DataAccessor dataAccessor) {
   this.userVerifier = userVerifier;
   this.dataAccessor = dataAccessor;
 }

 @Override
 public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
   String userIdToken = request.getParameter("idToken");
   if (userIdToken == null) {
     return;
   }
   Optional<String> optionalUserId = userVerifier.getUserIdByToken(userIdToken);
   if (!optionalUserId.isPresent()) {
     return;
   }
   String finalUserId = optionalUserId.get();
   if (!dataAccessor.isRegistered(finalUserId)){
     dataAccessor.registerUser(finalUserId);
   }
 }
}

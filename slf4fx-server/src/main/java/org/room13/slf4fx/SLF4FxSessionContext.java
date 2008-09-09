/*
 * Copyright 2008 Dmitry Motylev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.room13.slf4fx;

import org.apache.mina.statemachine.context.AbstractStateContext;

/**
 * TODO: Document the class
 */
public class SLF4FxSessionContext extends AbstractStateContext {
    private String _applicationId;
    private String _accessCode;

    public String getAccessCode() {
        return _accessCode;
    }

    public void setAccessCode(String accessCode) {
        _accessCode = accessCode;
    }

    public String getApplicationId() {
        return _applicationId;
    }

    public void setApplicationId(String applicationId) {
        _applicationId = applicationId;
    }
}
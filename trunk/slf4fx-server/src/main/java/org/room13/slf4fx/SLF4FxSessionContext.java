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
package com.jszczepankiewicz.s3.presigned;

import java.util.Map;

public interface FormProvider {
    Map<String, String> getFields();
}

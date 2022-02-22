package com.tencent.tsf.femas.common.tag.exception;

import java.text.MessageFormat;

public class TagEngineException extends RuntimeException {

    public TagEngineException(String messageFmt, Object... args) {
        super(new MessageFormat(messageFmt).format(args));
    }
}

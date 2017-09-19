package com.ycb.zprovider.aspect;

import java.lang.annotation.*;

/**
 * Created by zhuhui on 17-7-26.
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Action {

}

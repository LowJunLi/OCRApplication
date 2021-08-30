package com.example.ocrapplication;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import android.content.Context;
import android.content.SharedPreferences;

@ExtendWith(MockitoExtension.class)
public class LoginTest
{
    @Mock
    private Login mockLoginClass;

    /**
     * Set up environment for testing
     */
    @BeforeAll
    public static void setUp()
    {

    }

    /**
     * Set up environment for testing
     */
    @BeforeEach
    public void initTest()
    {

    }

    /**
     * {@link Login#checkEmptyField(String, String)}
     */
    @Test
    public void emptyUsernameIsInvalid()
    {
        assertFalse(mockLoginClass.checkEmptyField("", "123"));
    }



}
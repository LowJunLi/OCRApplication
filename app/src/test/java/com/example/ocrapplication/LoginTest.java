package com.example.ocrapplication;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;

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
    @Tag("UnitTest")
    public void emptyUsernameIsInvalid()
    {
        assertFalse(mockLoginClass.checkEmptyField("", "123"));
    }

    /**
     * {@link Login#checkEmptyField(String, String)}
     */
    @Test
    @Tag("UnitTest")
    public void emptyPasswordIsInvalid()
    {
        assertFalse(mockLoginClass.checkEmptyField("Low", ""));
    }



}
package com.example.sharedfood;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class UserTest {

    @Mock
    private UserAdapter.UserActionListener mockListener;

    @Mock
    private UserAdapter.UserViewHolder mockViewHolder;

    @InjectMocks
    private UserAdapter userAdapter;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    public void testUserCreation() {
        // Arrange
        String email = "test@example.com";
        boolean isBanned = false;
        Long tempBanTime = null;

        // Act
        User user = new User(email, isBanned, tempBanTime);

        // Assert
        assertEquals(email, user.getEmail());
        assertFalse(user.isBanned());
        assertNull(user.getTempBanTime());
    }

    @Test
    public void testUserAdapterItemCountWithMockListener() {
        // Arrange
        List<User> users = new ArrayList<>();
        users.add(new User("test@example.com", false, null));
        userAdapter = new UserAdapter(users, mockListener, null);

        // Mock behavior - just to ensure no interaction with listener affects the test
        doNothing().when(mockListener).onAction(any(User.class), anyString());

        // Act
        int itemCount = userAdapter.getItemCount();

        // Assert
        assertEquals(1, itemCount); // Verify that the adapter recognizes the single user
        verifyNoInteractions(mockListener); // Ensure no unintended interactions with listener
    }

    @Test
    public void testUserBanStatus() {
        // Arrange
        User user = new User("test@example.com", true, null);

        // Act
        user.setBanned(false);

        // Assert
        assertFalse(user.isBanned());
    }

    @Test
    public void testUserSetBanned() {
        // Arrange
        String email = "test@example.com";
        User user = new User(email, false, null);

        // Act
        user.setBanned(true);

        // Assert
        assertTrue(user.isBanned());
    }
}
package com.google.sps.data;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.maps.GeoApiContext;


@RunWith(JUnit4.class)
public class PlacesScorerFactoryTest {

    private static final GeoApiContext CONTEXT = GeoContext.getGeoApiContext();
    private static final UserVerifier USER_VERIFIER = mock(UserVerifier.class);
    private static final DataAccessor DATA_ACCESSOR = mock(DataAccessor.class);
    private PlacesScorerFactory placesScorerFactory;

    @Before
    public void setUp() throws Exception {
        placesScorerFactory = new PlacesScorerFactory(CONTEXT, USER_VERIFIER, DATA_ACCESSOR);
    }

    @Test
    public void create_emptyTokenId_returnScorerUnregisteredUser() throws Exception {
        when(USER_VERIFIER.getUserIdByToken("")).thenReturn(Optional.empty());
        assertTrue(placesScorerFactory.create("") instanceof PlacesScorerUnregisteredUser);
    }

    @Test
    public void create_validTokenId_returnScorerRegisteredUser() throws Exception {
        when(USER_VERIFIER.getUserIdByToken("token")).thenReturn(Optional.of("userId"));
        assertTrue(placesScorerFactory.create("token") instanceof PlacesScorerRegisteredUser);
    }
}

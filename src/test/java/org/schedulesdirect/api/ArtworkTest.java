package org.schedulesdirect.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schedulesdirect.api.Artwork.Size;

public class ArtworkTest {
	
	private EpgClient MOCK_CLNT = null;
	
	@BeforeClass
	public void setup() {
		MOCK_CLNT = mock(EpgClient.class);
		when(MOCK_CLNT.getBaseUrl()).thenReturn("http://testurl.com");
	}

	@Test
	public void validateArtworkParse() {
		String src = "{\"width\": \"135\",\"height\": \"180\",\"uri\": \"assets/p282288_b_v2_aa.jpg\",\"size\": \"Sm\",\"aspect\": \"3x4\",\"category\": \"Banner-L3\",\"text\": \"yes\",\"primary\": \"true\",\"tier\": \"Series\"}";
		JSONObject o = new JSONObject(src);
		Artwork a = new Artwork(o, MOCK_CLNT);
		
		assertEquals(135, a.getWidth());
		assertEquals(180, a.getHeight());
		assertTrue(a.getUri().endsWith("assets/p282288_b_v2_aa.jpg"));
		assertTrue(a.getSize() == Size.SMALL);
		assertEquals("3x4", a.getAspect());
		assertEquals("Banner-L3", a.getCategory());
		assertTrue(a.isText());
		assertEquals("Series", a.getTier());
	}
	
}

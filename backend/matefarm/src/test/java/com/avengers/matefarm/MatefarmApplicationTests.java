package com.avengers.matefarm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.avengers.matefarm.map.controller.MapController;
import com.avengers.matefarm.map.service.MapService;

@SpringBootTest
@WebMvcTest(MapController.class)
public class MatefarmApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MapService mapService;

    // @Test
    // void testRegCodeSelect() throws Exception {
    //     mockMvc.perform(get("/api/map/search").param("Locatadd_nm", "서울"))
    //             .andExpect(status().isOk());
    // }

}

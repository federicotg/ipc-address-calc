/*
 * Copyright (C) 2019 Federico Tello Gentile <federicotg@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.fede.calculator.money.RealProfit;
import org.fede.calculator.money.series.Investment;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public class RealProfitTest {
    
    private final List<Investment> investments;
    
    public RealProfitTest() throws IOException {
        
        this.investments = this.readExt("investments.json");
    }

    
        private List<Investment> readExt(String name) throws IOException {
        try (InputStream in = new FileInputStream("/home/fede/Sync/app-resources/" + name);) {
            ObjectMapper om = new ObjectMapper();

            return om.readValue(in, new TypeReference<List<Investment>>() {
            });
        }
    }
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void uva() {
    
        this.investments.stream().filter(i -> "1".equals(i.getId())).findFirst().ifPresent(this::testItem);
        
    }
    
    private void testItem(Investment i){
        
        RealProfit rp = new RealProfit(i);
        
    }
}

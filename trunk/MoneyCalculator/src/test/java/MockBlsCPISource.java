/*
 * Copyright (C) 2014 fede
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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.fede.calculator.money.NoSeriesDataFoundException;
import org.fede.calculator.money.bls.BlsCPISource;
import org.fede.calculator.money.bls.BlsResponse;

/**
 *
 * @author fede
 */
public class MockBlsCPISource implements BlsCPISource {

    @Override
    public BlsResponse getResponse(int year) throws NoSeriesDataFoundException, IOException {
        Map<Integer, String> strings = new HashMap<>();

        strings.put(2013, "{\"status\":\"REQUEST_SUCCEEDED\",\"responseTime\":722,\"message\":[],\"Results\":{\"series\":[{\"seriesID\":\"CUUR0000SA0\",\"data\":[{\"year\":\"2013\",\"period\":\"M13\",\"periodName\":\"Annual\",\"value\":\"232.957\",\"footnotes\":[{}]},{\"year\":\"2013\",\"period\":\"M12\",\"periodName\":\"December\",\"value\":\"233.049\",\"footnotes\":[{}]},{\"year\":\"2013\",\"period\":\"M11\",\"periodName\":\"November\",\"value\":\"233.069\",\"footnotes\":[{}]},{\"year\":\"2013\",\"period\":\"M10\",\"periodName\":\"October\",\"value\":\"233.546\",\"footnotes\":[{}]},{\"year\":\"2013\",\"period\":\"M09\",\"periodName\":\"September\",\"value\":\"234.149\",\"footnotes\":[{}]},{\"year\":\"2013\",\"period\":\"M08\",\"periodName\":\"August\",\"value\":\"233.877\",\"footnotes\":[{}]},{\"year\":\"2013\",\"period\":\"M07\",\"periodName\":\"July\",\"value\":\"233.596\",\"footnotes\":[{}]},{\"year\":\"2013\",\"period\":\"M06\",\"periodName\":\"June\",\"value\":\"233.504\",\"footnotes\":[{}]},{\"year\":\"2013\",\"period\":\"M05\",\"periodName\":\"May\",\"value\":\"232.945\",\"footnotes\":[{}]},{\"year\":\"2013\",\"period\":\"M04\",\"periodName\":\"April\",\"value\":\"232.531\",\"footnotes\":[{}]},{\"year\":\"2013\",\"period\":\"M03\",\"periodName\":\"March\",\"value\":\"232.773\",\"footnotes\":[{}]},{\"year\":\"2013\",\"period\":\"M02\",\"periodName\":\"February\",\"value\":\"232.166\",\"footnotes\":[{}]},{\"year\":\"2013\",\"period\":\"M01\",\"periodName\":\"January\",\"value\":\"230.280\",\"footnotes\":[{}]}]}]}}");
        strings.put(1964, "{\"status\":\"REQUEST_SUCCEEDED\",\"responseTime\":22,\"message\":[],\"Results\":{\"series\":[{\"seriesID\":\"CUUR0000SA0\",\"data\":[{\"year\":\"1964\",\"period\":\"M13\",\"periodName\":\"Annual\",\"value\":\"31.0\",\"footnotes\":[{}]},{\"year\":\"1964\",\"period\":\"M12\",\"periodName\":\"December\",\"value\":\"31.2\",\"footnotes\":[{}]},{\"year\":\"1964\",\"period\":\"M11\",\"periodName\":\"November\",\"value\":\"31.2\",\"footnotes\":[{}]},{\"year\":\"1964\",\"period\":\"M10\",\"periodName\":\"October\",\"value\":\"31.1\",\"footnotes\":[{}]},{\"year\":\"1964\",\"period\":\"M09\",\"periodName\":\"September\",\"value\":\"31.1\",\"footnotes\":[{}]},{\"year\":\"1964\",\"period\":\"M08\",\"periodName\":\"August\",\"value\":\"31.0\",\"footnotes\":[{}]},{\"year\":\"1964\",\"period\":\"M07\",\"periodName\":\"July\",\"value\":\"31.1\",\"footnotes\":[{}]},{\"year\":\"1964\",\"period\":\"M06\",\"periodName\":\"June\",\"value\":\"31.0\",\"footnotes\":[{}]},{\"year\":\"1964\",\"period\":\"M05\",\"periodName\":\"May\",\"value\":\"30.9\",\"footnotes\":[{}]},{\"year\":\"1964\",\"period\":\"M04\",\"periodName\":\"April\",\"value\":\"30.9\",\"footnotes\":[{}]},{\"year\":\"1964\",\"period\":\"M03\",\"periodName\":\"March\",\"value\":\"30.9\",\"footnotes\":[{}]},{\"year\":\"1964\",\"period\":\"M02\",\"periodName\":\"February\",\"value\":\"30.9\",\"footnotes\":[{}]},{\"year\":\"1964\",\"period\":\"M01\",\"periodName\":\"January\",\"value\":\"30.9\",\"footnotes\":[{}]}]}]}}");
        strings.put(1923, "{\"status\":\"REQUEST_SUCCEEDED\",\"responseTime\":18,\"message\":[],\"Results\":{\"series\":[{\"seriesID\":\"CUUR0000SA0\",\"data\":[{\"year\":\"1923\",\"period\":\"M13\",\"periodName\":\"Annual\",\"value\":\"17.1\",\"footnotes\":[{}]},{\"year\":\"1923\",\"period\":\"M12\",\"periodName\":\"December\",\"value\":\"17.3\",\"footnotes\":[{}]},{\"year\":\"1923\",\"period\":\"M11\",\"periodName\":\"November\",\"value\":\"17.3\",\"footnotes\":[{}]},{\"year\":\"1923\",\"period\":\"M10\",\"periodName\":\"October\",\"value\":\"17.3\",\"footnotes\":[{}]},{\"year\":\"1923\",\"period\":\"M09\",\"periodName\":\"September\",\"value\":\"17.2\",\"footnotes\":[{}]},{\"year\":\"1923\",\"period\":\"M08\",\"periodName\":\"August\",\"value\":\"17.1\",\"footnotes\":[{}]},{\"year\":\"1923\",\"period\":\"M07\",\"periodName\":\"July\",\"value\":\"17.2\",\"footnotes\":[{}]},{\"year\":\"1923\",\"period\":\"M06\",\"periodName\":\"June\",\"value\":\"17.0\",\"footnotes\":[{}]},{\"year\":\"1923\",\"period\":\"M05\",\"periodName\":\"May\",\"value\":\"16.9\",\"footnotes\":[{}]},{\"year\":\"1923\",\"period\":\"M04\",\"periodName\":\"April\",\"value\":\"16.9\",\"footnotes\":[{}]},{\"year\":\"1923\",\"period\":\"M03\",\"periodName\":\"March\",\"value\":\"16.8\",\"footnotes\":[{}]},{\"year\":\"1923\",\"period\":\"M02\",\"periodName\":\"February\",\"value\":\"16.8\",\"footnotes\":[{}]},{\"year\":\"1923\",\"period\":\"M01\",\"periodName\":\"January\",\"value\":\"16.8\",\"footnotes\":[{}]}]}]}}");
        return new ObjectMapper().readValue(strings.get(year), BlsResponse.class);
    }

}

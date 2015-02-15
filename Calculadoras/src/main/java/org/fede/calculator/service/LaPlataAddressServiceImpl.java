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
package org.fede.calculator.service;

import org.fede.calculator.lpadress.LaPlataAddress;
import org.fede.calculator.web.dto.LaPlataAddressDTO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 *
 * @author fede
 */
@Service
@Lazy
public class LaPlataAddressServiceImpl implements LaPlataAddressService {

    @Override
    public LaPlataAddressDTO getAddress(LaPlataAddressDTO dto) {
        final LaPlataAddress addr = LaPlataAddress.createAddressFromString(dto.getOriginalText());
        dto.setValid(addr.isValid());
        if (addr.isValid()) {
            dto.setLowerBoundary(addr.getLowerBoundary());
            dto.setUpperBoundary(addr.getUpperBoundary());
            dto.setNumber(addr.getNumber());
            dto.setStreet(addr.getStreet().toString());
        }
        return dto;
    }

}

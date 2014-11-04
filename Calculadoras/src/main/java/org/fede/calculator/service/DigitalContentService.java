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

import java.util.List;
import java.util.Set;
import org.fede.digitalcontent.dto.BoxLabelDTO;
import org.fede.digitalcontent.dto.DigitalContentDTO;
import org.fede.digitalcontent.dto.OpusDTO;
import org.fede.digitalcontent.dto.VenueDTO;
import org.fede.digitalcontent.dto.VenueDetailDTO;
import org.fede.digitalcontent.model.StorageBox;

/**
 *
 * @author fede
 */
public interface DigitalContentService {
    
    Iterable<StorageBox> getAllBoxes();
    
    List<DigitalContentDTO> getFullReport();
    List<DigitalContentDTO> getBoxReport(String box);
    List<DigitalContentDTO> getComposerReport(String composerName);
    List<DigitalContentDTO> getOpusReport(String opusName);
    List<DigitalContentDTO> getVenueReport(String venueName);
    List<DigitalContentDTO> getOpusTypeReport(String name);
    BoxLabelDTO getBoxLabel(String boxName);
    List<BoxLabelDTO> getEveryBoxLabel();
    List<VenueDTO> getVenues();
    VenueDetailDTO getVenueDetail(String venueName);
    List<OpusDTO> unseenBy(String personName);
    List<OpusDTO> unavailableInHD();
}

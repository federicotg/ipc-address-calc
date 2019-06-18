package org.fede.calculator.money.series;

import java.util.Set;

/*
 * Copyright (C) 2016 Federico Tello Gentile <federicotg@gmail.com>
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
/**
 *
 * @author Federico Tello Gentile <federicotg@gmail.com>
 */
public enum InvestmentType {
    PF {
        @Override
        public boolean isValid(InvestmentEvent in, InvestmentEvent out, InvestmentAsset asset) {
            return out != null
                    && in.getDate().before(out.getDate())
                    && in.getCurrency().equals(asset.getCurrency())
                    && in.getCurrency().equals(out.getCurrency())
                    && in.getAmount().compareTo(out.getAmount()) < 0;
        }
    },
    USD {
        @Override
        public boolean isValid(InvestmentEvent in, InvestmentEvent out, InvestmentAsset asset) {
            return asset.getCurrency().equals("USD");
        }
    },
    FCI {
        @Override
        public boolean isValid(InvestmentEvent in, InvestmentEvent out, InvestmentAsset asset) {
            return true;
        }
    },
    XAU {
        @Override
        public boolean isValid(InvestmentEvent in, InvestmentEvent out, InvestmentAsset asset) {
            return "XAU".equals(asset.getCurrency());
        }
    }/*,
    LETE {
        @Override
        public boolean isValid(InvestmentEvent in, InvestmentEvent out, InvestmentAsset asset) {
            return "LETE".equals(asset.getCurrency());
        }        
    },
    LECAP {
        @Override
        public boolean isValid(InvestmentEvent in, InvestmentEvent out, InvestmentAsset asset) {
            return "LECAP".equals(asset.getCurrency());
        }
    }*/,
    BONO {
        @Override
        public boolean isValid(InvestmentEvent in, InvestmentEvent out, InvestmentAsset asset) {
            return Set.of("AY24", "LETE", "LECAP", "USD").contains(asset.getCurrency());
        }
    }/*,
    ON {
        @Override
        public boolean isValid(InvestmentEvent in, InvestmentEvent out, InvestmentAsset asset) {
            return "ON".equals(asset.getCurrency());
        }
    }*/;

    public abstract boolean isValid(InvestmentEvent in, InvestmentEvent out, InvestmentAsset asset);

}

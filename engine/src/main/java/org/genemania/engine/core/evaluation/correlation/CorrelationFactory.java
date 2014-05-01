/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.genemania.engine.core.evaluation.correlation;

public class CorrelationFactory {
	
	public enum CorrelationType {
		PEARSON, SPEARMAN, PEARSON_RANK, MUTUAL_INFORMATION, PEARSON_BIN_LOG_NORM, PEARSON_BIN_LOG_NO_NORM
	}
	
	public static Correlation getCorrelation(CorrelationType correlationType, 
			                                 MutualInformationData MIData ){
		switch ( correlationType ){
			case PEARSON: 
				return new Pearson();
			case SPEARMAN: 
				return new Spearman();
			case PEARSON_RANK:
				return new PearsonColumnRank();
			case MUTUAL_INFORMATION:
				if ( !MIData.useBinning() ){
					return new MutualInformationBinary();
				} else {
					if ( MIData.isEqualElementBin() ){
						return new MutualInformationEqualElem( MIData.getSizeType() );
					} else {
						return new MutualInformationEqualRange( MIData.getSizeType() );
					}
				}
			case PEARSON_BIN_LOG_NORM:
				return new LogScaledPearsonBinaryWithNormalization();
			case PEARSON_BIN_LOG_NO_NORM:
				return new LogScaledPearsonBinaryNoNormalization();
			default:
				throw new RuntimeException( "Unknown correlation type:" + correlationType.getClass().getName() );
		}
	}
}

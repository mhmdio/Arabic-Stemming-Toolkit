/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is DFI0.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   B.T. Dincer (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   
 */
package org.terrier.matching.models;

/** Implementation of the basic Divergence from Independence model. 
 * For details on this model, please see 
 * IRRA at TREC 2009: Index Term Weighting Based on Divergence From Independence Model
 * B.T. Dincer, I. Kocaba&#353; and B. Karo&#287;lan. In Proceedings of TREC 2009.
 * @author B Taner Dincer and Craig Macdonald
 * @since 3.5
 */
public class DFI0 extends WeightingModel {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getInfo() {
		return "DFI0";
	}

	@Override
	public double score(double tf, double docLength)
	{
		final double eij = super.termFrequency * (docLength / super.numberOfTokens);
		return keyFrequency * Idf.log(1+ (tf - eij)/Math.sqrt(eij) );
	}

	@Override
	public double score(double tf, double docLength, double n_t, double F_t,
			double keyFrequency) 
	{
		final double eij =  F_t * (docLength / super.numberOfTokens);
		return keyFrequency * Idf.log(1+ (tf - eij)/Math.sqrt(eij) );
	}

}

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

/*
 * Custom matrix package for GeneMANIA. Not intended to be
 * complete or general, but optimized for use-cases of interest.
 * 
 * 
 * 
 * TODO:
 * 
 *   * sometimes RuntimException sometimes MatricksException, consolidate
 *   * only some methods declare the MatricksException (probably from when it was
 *     a checked exception).
 *   * do we every want a checked exception returned? i say not, user beware.
 *   
 *   * sometimes use of matricks.Vector and sometimes uses of double [], make
 *     more consistent
 * 
 */

package org.genemania.engine.matricks;
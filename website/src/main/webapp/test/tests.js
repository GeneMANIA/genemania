testSuite(function(){
	
	////#############################################################################
	////TEST testModuleS: add new tests here
	////#############################################################################
	
	////#############################################################################
	////SEARCH PAGE TESTS
	////#############################################################################
	
	testModule("Search Page");
	
	testCase("Organism choices", function(){
		expect(2);
		
		// Test number of organisms
		same(gm$("#species_select option").size(), NUMBER_OF_ORGANISMS, "Number of query organisms");
		
		// Test default organism
		same(gm$("#species_select").val(), "4", "Default organism");
		
		nextTestCase();
	});
	
	testCase("Type in autocomplete", function(){ // TODO: Test sometimes fails incorrectly for some reason
		// To change the test, simply change the test_string
		var test_string = 'as';
		gm$("#species_text").trigger("focus");
		wait(function(){
			setOrganismText(test_string, function(){
				// Check that there is at least one result
				var result_num = gm$(".ac_results li").size();
				ok(result_num > 0, "'" + test_string + "' produced " + result_num + " auto-complete results");
				// Saves the first result for later test
				var first_result = gm$(".ac_results li:first").text();
				var pass = true;
				// Check that each result contains the test_string
				gm$(".ac_results li").each(function(i, li){
					var result = gm$(this).text();
					ok(result.indexOf(test_string) != -1, result + " contains " + test_string);
					if (result.indexOf(test_string) == -1){
						pass = false;
					}
				});
				if (pass){
					// Clicks on the first result and verifies it
					gm$('.ac_results li:first').trigger('click');
					//getOrganismName gets the non-scientific name from the string, e.g. "human", "fly", etc.
					org = getOrganismName(gm$("#species_text").val());
					same(org, getOrganismName(first_result), "Switched to first result, " + first_result);
					nextTestCase();
				}
				else{
					nextTestCase();
				}
			});
		});
	});
	
	testCase("Show/hide advanced options", function(){
		expect(2);
		openAdvancedOptions(function(){
			ok(gm$("#networks_section").is(':visible'), "Advanced options should be visible");
			closeAdvancedOptions();
			ok(!gm$("#networks_section").is(':visible'), "Advanced options should be hidden");
			nextTestCase();
		});
	});
	
	testCase("Advanced options is not accessible while changing organism", function(){
		expect(3);
		openAdvancedOptions(function(){
			setOrganismById(MOUSE_ID);
			ok(!gm$("#networks_toggle").is(":visible"), "Networks toggle should be hidden");
			ok(!gm$("#networks_list").is(":visible"), "Networks list should be hidden");
			ok(gm$("#networks_section_loading").is(":visible"), "Networks loading icon should be visible");
			nextTestCase();
		});
	});
	
	testCase("Networks panel", function(){
		openAdvancedOptions(function(){
			// Check for Network Weighting buttons
			ok(gm$(".network_weighting_group").is(':visible'), "Network weighting buttons exist");
			
			// Check for Number of Gene Results option
			ok(gm$("#threshold").is(':visible'), "Number of gene results option exists");
			
			var rand = Math.ceil(Math.random() * (gm$(".query_networks:first > div").size() - 2)) + 1; // Random number between 1 and the number of networks, exclusive
		
			var expandFunc = function(index){ // Expand network, check description
				gm$(".query_networks:first > div:nth-child(" + index + ")").click();
				var id_num = gm$(".query_networks:first > div:nth-child(" + index + ")").attr('id');
				ok(gm$("#descriptionFor" + id_num).is(':visible'), "Description for network id " + id_num + " (	index " + index + ") should be visible");
			};
			
			// Check first and last, along with one random network in the middle
			expandFunc(1);
			expandFunc(rand);
			expandFunc(gm$(".query_networks:first > div").size());
			
			nextTestCase();
		});
	});
	
	testCase("Select all networks", function(){
		openAdvancedOptions(function(){
			selectAndCheckAllNetworks();
			wait(function(){
				nextTestCase();
			});
		});
	});
	
	testCase("Deselect all networks", function(){	
		openAdvancedOptions(function(){
			deselectAndCheckAllNetworks();
			wait(function(){
				nextTestCase();
			});
		});
	});
	
	testCase("Select default networks", function(){
		openAdvancedOptions(function(){
			selectAndCheckDefaultNetworks();
			wait(function(){
				nextTestCase();
			});
		});
	});
	
	testCase("Sort network by first author", function(){	
		sortAndCheckNetworkByFirstAuthor(function(){
			nextTestCase();
		});
	});
	
	testCase("Sort network by last author", function(){	
		sortAndCheckNetworkByLastAuthor(function(){
			nextTestCase();
		});
	});
	
	testCase("Sort network by publication date", function(){
		sortAndCheckNetworkByDate(function(){
			nextTestCase();
		});
	});
	
	testCase("Sort network by size", function(){
		sortAndCheckNetworkBySize(function(){
			nextTestCase();
		});
	});
	
	testModule("Gene Validation");
	////#############################################################################
	////GENE VALIDATION TESTS
	////#############################################################################
	
	testCase("Warning tool-tips", function(){
		expect(2);
		if ($.browser.msie){
			// Skip this test if the browser is IE; it doesn't work in IE.
			ok(true, "This test does not currently work in IE.");
			nextTestCase();
		}
		else{
			setGenes("asdf", function(){
				// Check for the warning tool-tip above gene area
				waitForVisible(gm$("#gene_error .ellipsis_text"), function(){
					same(removeWhiteSpace(gm$("#gene_error .ellipsis_text").text()), "None of the symbols entered were recognized.", "Check gene warning message");
				}, 100);
				
				openAdvancedOptions(function(){
					gm$("#network_selection_select_none").trigger('click');
					
					// Check for the warning tool-tip above network panel
					waitForVisible(gm$("#networks_section_error.warning_msg"), function(){
						same(removeWhiteSpace(gm$("#networks_section_error.warning_msg").text()), "With no networks selected, the default networks will be used.", "Check network warning message");
						nextTestCase();
					}, 100);
				});
			});
		}
	});
	
	testCase("No genes", function(){
		expect(2);
		setGenes("");
		// Verify Q-tip when clicking Go button
		clickGo();
		waitForVisible(gm$(".qtip"), function(){
			ok(gm$(".qtip.qtip-red.qtip-active").is(":visible"), "Warning for no genes (after clicking Go) should be visible");
			// Verify warning above gene area
			verifyGeneAreaWarningVisible();
			nextTestCase();
		}, 40);
	});
	
	testCase("Default genes button", function(){
		function testOrg(organism_id){
			setOrganismById(organism_id, function(){
				same(gm$("#species_text").val(), default_organisms[organism_id], "Organism should be set to " + default_organisms[organism_id]);
				clickDefaultGenes(function(){
					verifyGeneAreaWarningNotVisible();
					
					// remove whitespace (String.replace)
					// to lower case (String.toLowerCase)
					
					same(gm$("#gene_text").val().replace(/ /g, "").toLowerCase(), default_genes[organism_id].replace(/ /g, "").toLowerCase(), "Default genes for " + gm$("#species_text").val() + " should be correct");
				
					if( organism_id < NUMBER_OF_ORGANISMS ){
						testOrg(organism_id + 1);
					} else {
						// we've finished all organisms
						setTimeout(function(){nextTestCase()}, 1000);
					}
				});
			});	// Switch to next organism
		}
		
		testOrg(1);
		
	});
	
	testCase("Default networks", function(){
		var org = Math.ceil(Math.random() * NUMBER_OF_ORGANISMS);
		setOrganismById(org, function(){
			ok(true, "Organism is set to " + gm$("#species_text").val());
			checkDefaultNetworks(org);
			setTimeout(function(){nextTestCase()}, 1000);
		});
	});
	
	testCase("Large gene list - 400 genes", function(){
		largeGeneList(400, function(list){
			setGenes(list, function(){
				// Verify that the text box has the list
				same(gm$("#gene_area").val(), list, "The large gene list should fit the gene text box");
				nextTestCase();
			});
		});
	});
	
	testCase("One gene, one synonym", function(){
		if ($.browser.msie){
			// Skip this test if the browser is IE; it doesn't work in IE.
			ok(true, "This test does not currently work in IE.");
			nextTestCase();
		}
		else{
			checkGeneValidation("11144\n" +
					" DMC1H", function(){
				// Verify Q-tip content
				verifyGeneTooltipVisible();
				verifyGeneTooltipTitle("There is a duplicated gene symbol.");
				verifyGeneTooltipContent("Synonyms");
				
				// Verify warning above gene area
				verifyGeneAreaWarningVisible();
				verifyGeneAreaWarningText("There is a duplicated gene symbol.");
				nextTestCase();
			});
		}
	});
	
	testCase("One gene, multiple gene synonyms", function(){
		if ($.browser.msie){
			// Skip this test if the browser is IE; it doesn't work in IE.
			ok(true, "This test does not currently work in IE.");
			nextTestCase();
		}	
		else{
			checkGeneValidation("LIM15\n" +
					"11144\n" +
					"DMC1H\n" +
					"HsLim15\n" +
					"Q14565", function(){
				// Verify Q-tip content
				verifyGeneTooltipVisible();
				verifyGeneTooltipTitle("There are duplicated gene symbols.");
				verifyGeneTooltipContent("Synonyms");
				
				// Verify warning above gene area
				verifyGeneAreaWarningVisible();
				verifyGeneAreaWarningText("There are duplicated gene symbols.");
				nextTestCase();
			});
		}
	});
	
	testCase("Multiple genes, one with synonyms", function(){
		if ($.browser.msie){
			// Skip this test if the browser is IE; it doesn't work in IE.
			ok(true, "This test does not currently work in IE.");
			nextTestCase();
		}
		else{
			checkGeneValidation("pcna\n" +
					"1\n" +
					"mlh1\n" +
					"LIM15\n" +
					"11144\n" +
					"DMC1H\n" +
					"HsLim15\n" +
					"Q14565", function(){
				// Verify Q-tip content
				verifyGeneTooltipVisible();
				verifyGeneTooltipTitle("There are duplicated gene symbols.");
				verifyGeneTooltipContent("Synonyms");
		
				// Verify warning above gene area
				verifyGeneAreaWarningVisible();
				verifyGeneAreaWarningText("There are duplicated gene symbols.");
				nextTestCase();
			});
		}
	});
	
	testCase("Synonyms, invalids, and duplicates, oh my", function(){
		if ($.browser.msie){
			// Skip this test if the browser is IE; it doesn't work in IE.
			ok(true, "This test does not currently work in IE.");
			nextTestCase();
		} else{
			checkGeneValidation("DMC1H\n" +
					"11144\n" +
					"ASDF\n" +
					"DMC1H", function(){
				// Verify Q-tip content
				verifyGeneTooltipVisible();
				verifyGeneTooltipTitle("There are multiple warnings.");
				verifyGeneTooltipContent("Synonyms");
				verifyGeneTooltipContent("Duplicate");
				verifyGeneTooltipContent("Unrecognized");
				
				// Verify warning above gene area
				verifyGeneAreaWarningVisible();
				verifyGeneAreaWarningText("There are multiple warnings.");
				
				// Verify the validation icons
				// Invalid icon = ?
				same(gm$("div[gene='asdf']").attr("className"), "icon invalid", "ASDF's icon should be a question mark");
				
				// Synonym icon = !
				same(gm$("div[gene='11144']").attr("className"), "icon synonym", "11144's icon should be an exclamation mark");
				
				// Duplicate icon = !
				same(gm$("div[gene='dmc1h']").first().attr("className"), "icon valid", "First DMC1H's icon should be a checkmark");
				same(gm$("div[gene='dmc1h']").last().attr("className"), "icon duplicate", "Second DMC1H's icon should be an exclamation mark");
				nextTestCase();
			});
		}
	});
	
	testCase("Lots and lots of invalid genes", function(){
		if ($.browser.msie){
			// Skip this test if the browser is IE; it doesn't work in IE.
			ok(true, "This test does not currently work in IE.");
			nextTestCase();
		} else{
			checkGeneValidation("asdf\n" +
					"qqqqqqq\n" +
					"hihih12345\n" +
					"qwertyuiop\n" +
					"InVaLiD.GeNe\n" +
					"3.14159\n" +
					"_hi\n" +
					";mlh1\n" +
					";;mlh1\n" +
					"!@#$%^mlh1&*()\n" +
					"(pcna)", function(){
				verifyGeneTooltipVisible();
				verifyGeneTooltipTitle("None of the symbols entered were recognized.");
				verifyGeneTooltipContent("Unrecognized");
				nextTestCase();
			});
		}
	});
	
	testCase("Valid genes with surrounding whitespace", function(){
		if ($.browser.msie){
			// Skip this test if the browser is IE; it doesn't work in IE.
			ok(true, "This test does not currently work in IE.");
			nextTestCase();
		} else{
			checkGeneValidation("    pcna    \nmlh1     \n      1", function(){
				verifyGeneAreaWarningNotVisible();
				nextTestCase();
			}, valid = true)
		}
	});
	
	testCase("Switch organisms; genes should not change", function(){
		expect(1);
		
		var test_genes = "pcna\n1\nmlh1\n;invalid ";
		var expected_genes = "pcna; 1; mlh1; ;invalid"; // Text box converts the newline characters to semicolons
		setGenes(test_genes, function(){
			setOrganismById(MOUSE_ID, function(){
				same(gm$("#gene_text").val(), expected_genes, "Genes did not change");
				nextTestCase();
			});
		});
	});
	
	testModule("Run Query");
	////#############################################################################
	////RUN QUERY TESTS
	////#############################################################################
	
	testCase("Valid genes are validated on the results page", function(){
		var genes = "pcna\n" +
					"xrcc2\n";
		setGenes(genes, function(){
			runQuery(function(){
				gm$("#gene_text").trigger("focus");
				verifyGeneAreaWarningNotVisible();
				nextTestCase();
			});
		});
	});
	
	testCase("Invalid genes are invalidated on the results page", function(){
		var genes = "pcna\n" +
					"xrcc2\n" +
					"invalidgene\n";
		setTimeout(function(){
			setGenes(genes, function(){
				runQuery(function(){
					gm$("#gene_text").trigger("focus");
					verifyGeneAreaWarningVisible();
					nextTestCase();
				});
			});
		}, 1000);
	});
	
	testCase("Run query with genes that have special characters", function(){
		expect(1);
		setOrganismById(YEAST_ID, function(){
			setGenes("ade5,7\n" +
					"arg5,6\n" +
					"dur1,2", function(){
				runQuery(function(){
					gm$("#gene_text").trigger("focus");
					verifyGeneAreaWarningNotVisible();
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Run query with genes that have special characters 2", function(){
		expect(1);
		setOrganismById(ARABIDOPSIS_ID, function(){
			setGenes("NAP1;2\n" +
					"EIF2_GAMMA\n" +
					"F2D10.35\n" +
					"ARA-2\n", function(){
				runQuery(function(){
					gm$("#gene_text").trigger("focus");
					verifyGeneAreaWarningNotVisible();
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Run query with gene id instead of symbol", function(){
		setOrganismById(YEAST_ID, function(){
			setGenes("851396", function(){
				runQuery(function(){
					gm$("a[href='#genes_tab']").click();
					// Check that the gene is there
					var gene_exists = false;
					gm$("div[class='gene_name']").each(function(i){
						if (gm$(this).text().toUpperCase() == 'STE7 (851396)'){
							gene_exists = true;
						}
					});
					ok(gene_exists, "The gene is shown correctly in the table as STE7 (851396)");
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Append genes to the default genes and run query", function(){
		var genes = gm$("#species_select option[value=" + gm$("#species_select").val() + "]").attr("defgenes") + "\n A1BG\n SHH";
		var gene_A1BG_exists = false;
		var gene_shh_exists = false;
		setGenes(genes, function(){
			runQuery(function(){
				gm$("a[href='#genes_tab']").click();	
				// Check that the added genes are there
				gm$("div[class='gene_name']").each(function(i){
					if (gm$(this).text().toUpperCase() == 'A1BG '){
						gene_A1BG_exists = true;
					}
					else if (gm$(this).text().toUpperCase() == 'SHH '){
						gene_shh_exists = true;
					}
				});
				ok(gene_A1BG_exists && gene_shh_exists, "Both genes should be shown in the results page");
				nextTestCase();
			});
		});
	});
	
	testCase("Run one query, then switch to another organism and run again", function(){ 
		runSpecificDefaultQuery(HUMAN_ID, function(){
			checkDefaultNetworks(HUMAN_ID);
			wait(function(){
				closeAdvancedOptions();
				wait(function(){
					setOrganismById(YEAST_ID, function(){
						setGenes("851396", function(){
							runQuery(function(){
								openAdvancedOptions(function(){
									ok( gm$("#results_page").size() > 0 , "Query was run successfully");
									checkDefaultNetworks(YEAST_ID);
									checkNetworkPanelMatchesQueryNetworks();
									nextTestCase();
								});
							});
						});
					});
				});
			});
		});
	});

	testCase("Run a query with just one non-default network and check it", function(){
		var random_network_name;
		openAdvancedOptions(function(){
			selectOneRandomNetwork(function(network_id){
				random_network_name = gm$(".query_network > input[type='checkbox'][value="+ network_id +"]").parent().children('label').text();
				runSpecificDefaultQuery(DEFAULT_ID, function(){
					gm$("#networks_tab_expand_top_level").click();
					wait(function(){
						var network_object = gm$(".checktree_network:contains(" + random_network_name + ")");
						ok(network_object.is(":visible"), random_network_name + " should be in the list of result networks in the networks tab");
						nextTestCase();
					});
				});
			});
		});
	});
	
	testCase("Run a query and reload the page", function(){
		runDefaultQuery(function(){
			wait(function(){
				var old_organism = gm$("#species_text").val();
				var old_genes = gm$("#gene_area").text();
				var old_node_scores = sortNodesByScore(vis().nodes());
				
				reloadResultsPage(function(){
					// Check that the cytoscape graph as well as the organism/gene fields remain the same
					same(gm$("#species_text").val(), old_organism, "The organism should not have changed after reloading");
					same(gm$("#gene_area").text(), old_genes, "The genes should not have changed after reloading");
					
					// Check the cytoscape graph, ignore x and y locations
					
					same(sortNodesByScore(vis().nodes()), old_node_scores, "The cytoscape node scores should not have changed after reloading");
					
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Run a query, change the organism, and reload the page", function(){
		runDefaultQuery(function(){
			wait(function(){
				var old_organism = gm$("#species_text").val();
				var old_genes = gm$("#gene_area").text();
				var old_organism_id = gm$("#species_select").val();
				if (old_organism_id == 1){
					setOrganismById(2, function(){
						reloadResultsPage(function(){
							// Check that the cytoscape graph as well as the organism/gene fields remain the same
							// TODO: Check the cytoscape graph
							same(gm$("#species_text").val(), old_organism, "The organism should not have changed after reloading");
							same(gm$("#gene_area").text(), old_genes, "The genes should not have changed after reloading");
							nextTestCase();
						});
					});
				}
				else{
					setOrganismById(1, function(){
						reloadResultsPage(function(){
							// Check that the cytoscape graph as well as the organism/gene fields remain the same
							// TODO: Check the cytoscape graph
							same(gm$("#species_text").val(), old_organism, "The organism should not have changed after reloading");
							same(gm$("#gene_area").text(), old_genes, "The genes should not have changed after reloading");
							nextTestCase();
						});
					});
				}
			});
		});
	});
	
	
	testModule("Results Page");
	////#############################################################################
	////RESULTS PAGE TESTS
	////#############################################################################
	
	testCase("Results page has a link back to the query screen", function(){
		runDefaultQuery(function(){
			ok(gm$("a > #logo").is(":visible"), "Logo link should be visible");
			nextTestCase();
		});
	});
	
	testCase("Hide/show results panel", function(){
		runDefaultQuery(function(){
			ok(gm$("#side_bar").is(":visible"), "Side bar should be visible");
			gm$("#side_bar_toggle_open").click();
			ok(!gm$("#side_bar").is(":visible"), "Side bar should be hidden");
			gm$("#side_bar_toggle_closed").click();
			ok(gm$("#side_bar").is(":visible"), "Side bar should be visible again");
			nextTestCase();
		});
	});
	
	testCase("Networks legend matches networks tab", function(){
		runDefaultQuery(function(){
			gm$("#menu_legend").click();
			gm$("#legend .content > ul > li").each(function(){
				// Get color and label of the current legend element
				var label = gm$(this).children("label").text();
				var colour = gm$(this).children('.legend_square').css("background-color");
				var pass = false;
				
				// Make sure that the color and label match some network group in the networks panel
				gm$('#networks_tab .checktree_top_level').each(function(){
					if (colour == gm$(this).children('.per_cent_bar').children('.bar').css('background-color') && label == gm$(this).children('.label').children('.network_name').text()){
						ok(true, label + ", " + colour + ", was matched");
						pass = true;
						return false; // break out of .each loop
					}
					//else 
						//console.log (colour + " " + label + " != " + gm$('#networks_tab .checktree_top_level').children('.per_cent_bar').children('.bar').css('background-color') + " " + gm$(this).children('.label').children('.network_name').text());
				});
				
				if (!pass){
					ok(false, label + " was not matched in the networks panel");
				}
			});
			nextTestCase();
		});
	});
	
	testCase("Show/hide networks legend", function(){
		runDefaultQuery(function(){
			ok(!gm$("#legend").is(":visible"), "Legend should be invisible by default");
			gm$("#menu_legend").click();
			ok(gm$("#legend").is(":visible"), "Legend should be visible now");
			gm$("#menu_legend").click();
			waitForNotVisible(gm$("#legend"), function(){
				ok(!gm$("#legend").is(":visible"), "Legend should be invisible again");
				nextTestCase();
			});
		});
	});
	
	testCase("Networks tab expansion", function(){
		runDefaultQuery(function(){
			gm$(".checktree_top_level:first > .arrow").click();
			gm$(".checktree_top_level:first > ul > li").each(function(i){		// Iterate through each network of the first group
				if (gm$(this).is(":visible")){
					// Check for partial node selection
					gm$(this).children(".arrow").click();
					
					var network_id = gm$(this).attr('id');
					var id_num = network_id.substring(7); // Separate "XYZ" number from the string "networkXYZ"
					
					// Check for individual network descriptions
					ok(gm$("#networkDescription" + id_num).is(':visible'), "Description for network id " + id_num + " should be visible");
				}
				else{
					ok(false, "The top-level arrow did not expand properly when clicked");
				}
			});
			nextTestCase();
		});
	});
	
	testCase("Each network description has a source", function(){
		runDefaultQuery(function(){
			// Open all the network groups, then all the networks
			gm$(".checktree_top_level").click();
			gm$(".checktree_network").click();
			
			var source_exists = true;
			gm$(".checktree_network > ul").each(function(i){
				ok(gm$(this).text().indexOf("Source:") != -1, gm$(this).parent().attr("id") + " should have a source");
			});
			nextTestCase();
		});
	});
	
	testCase("Sort networks tab by name", function(){
		runDefaultQuery(function(){
			gm$("#networks_tab_sort_by_name").click();
			gm$(".checktree_top_level .arrow").click();
			
			// Use a nested loop for each network group, check it is sorted
			var checked = false;
			var sorted = true;
			var prev_network_name = "";
			gm$(".checktree_top_level > ul").each(function(i){
				gm$(this).find('.network_name').each(function(j){
					 ok(gm$(this).text().toLowerCase() >= prev_network_name, gm$(this).text() + " is below " + prev_network_name);
					 prev_network_name = gm$(this).text().toLowerCase();
				});
				prev_network_name = "";
			});
			nextTestCase();
		});
	});
	
	testCase("Sort networks tab by percentage weight", function(){
		runDefaultQuery(function(){
			gm$("#networks_tab_sort_by_weight").click();
			gm$(".checktree_top_level .arrow").click();
			
			// Use a nested loop for each network group, check it is sorted
			var prev_network_percent = 100;
			gm$(".checktree_top_level > ul").each(function(i){
				gm$(this).find('.per_cent_text').each(function(j){
					var current_network_percent = parseFloat(gm$(this).text());
					ok (current_network_percent <= prev_network_percent, current_network_percent + " is below " + prev_network_percent);
					prev_network_percent = current_network_percent;
				});
				prev_network_percent = 100; // Reset the prev_network_percent at the end of every network group
			});
			nextTestCase();
		});
	});
	
	testCase("Expand all in networks tab", function(){
		runDefaultQuery(function(){
			gm$("#networks_tab_expand_all").click();
			
			gm$(".checktree_top_level").each(function(){		// Check that all top level network groups are visible
				ok(gm$(this).is(":visible"), "Top level networks should be visible");
			});
	
			gm$(".checktree_network").each(function(){		// Check that all networks are visible
				ok(gm$(this).is(":visible"), "Networks should be visible");
			});
	
			gm$(".checktree_network > ul").each(function(){		// Check that all network descriptions are visible
				ok(gm$(this).is(":visible"), "Network description should be visible");
			});
			
			nextTestCase();
		});
	});
	
	testCase("Expand only top level in networks tab", function(){
		runDefaultQuery(function(){
			gm$("#networks_tab_expand_top_level").click();
	
			gm$(".checktree_top_level").each(function(){		// Check that all top level network groups are visible
				ok(gm$(this).is(":visible"), "Top level networks should be visible");
			});
	
			gm$(".checktree_network").each(function(){		// Check that all networks are visible
				ok(gm$(this).is(":visible"), "Networks should be visible");
			});
	
			gm$(".checktree_network > ul").each(function(){		// Check that all network descriptions are not visible
				ok(!gm$(this).is(":visible"), "Network description should be not visible");
			});
			
			nextTestCase();
		});
	});
	
	testCase("Expand none in networks tab", function(){
		runDefaultQuery(function(){
			gm$("#networks_tab_expand_none").click();
			
			gm$(".checktree_top_level").each(function(){		// Check that all top level network groups are visible
				ok(gm$(this).is(":visible"), "Top level networks should be visible");
			});
	
			gm$(".checktree_network").each(function(){		// Check that all networks are not visible
				ok(!gm$(this).is(":visible"), "Networks should be not visible");
			});
	
			gm$(".checktree_network > ul").each(function(){		// Check that all network descriptions are not visible
				ok(!gm$(this).is(":visible"), "Network description should be not visible");
			});
	
			nextTestCase();
		});
	});
	
	testCase("Deselect the first network group", function(){
		runDefaultQuery(function(){
			// This test depends on top level expand button working
			gm$("#networks_tab_expand_top_level").click();
			
			ok(allNetworkCheckboxesAreChecked(true), "All network checkboxes are checked initially");
			
			gm$(".checktree_top_level > .checkbox:first").click(); // Click the first network group's checkbox
			gm$(".checktree_top_level:first > ul > li").each(function(){ // Verify the children are unchecked
				ok(!gm$(this).children('.checkbox').is(':checked'), gm$(this).children('.label').children('.network_name').text() + " should now be unchecked");
			});
			gm$(".checktree_top_level > .checkbox:first").click(); // Click the first network group's checkbox
			gm$(".checktree_top_level:first > ul > li").each(function(){ // Verify the children are checked
				ok(!gm$(this).children('.checkbox').is(':checked'), gm$(this).children('.label').children('.network_name').text() + " should now be checked");
			});
			nextTestCase();
		});
	});
	
	testCase("Genes tab expansion", function(){
		runDefaultQuery(function(){
			gm$("a[href='#genes_tab']").click();
			gm$("#genes_widget > li > .arrow").click();
			ok(gm$("#genes_widget .description").is(":visible"), "Gene descriptions should be expandable and visible");
			nextTestCase();
		});
	});
	
	testCase("In genes tab, links for more gene information exist", function(){
		runDefaultQuery(function(){
			gm$("a[href='#genes_tab']").click();
			gm$(".gene_valid_true > .arrow").click();
			
			gm$(".gene .external_link").each(function(){
				ok(gm$(this).is(":visible"), "External link should be visible");
			});
			
			nextTestCase();
		});
	});
	
	testCase("Sort genes tab by name", function(){
		runDefaultQuery(function(){
			gm$("a[href='#genes_tab']").click();
			gm$("#genes_tab_sort_by_name").click();
			var sorted = true;
			var prev_gene = "";
			var current_gene;
			gm$(".gene_name").each(function(){
				current_gene = gm$(this).text().toLowerCase();			
				ok(current_gene >= prev_gene, current_gene + " is after " + prev_gene);
				prev_gene = current_gene;
			})
			nextTestCase();
		});
	});
	
	testCase("Sort genes tab by score", function(){
		runDefaultQuery(function(){
			gm$("a[href='#genes_tab']").click();
			gm$("#genes_tab_sort_by_score").click();
			var sorted = true;
			var prev_score = 100;
			var current_score = 0;
			gm$(".score_text").each(function(){
				current_score = parseFloat(gm$(this).text());
				ok(current_score <= prev_score, current_score + " is after " + prev_score);
				prev_score = current_score;
			})
			nextTestCase();
		});
	});
	
	
	testCase("Expand all in the genes tab", function(){
		runDefaultQuery(function(){
			gm$("a[href='#genes_tab']").click();
			gm$("#genes_tab_expand_all").click();
	
			gm$(".gene_valid_true .text").each(function(){		// Check that all gene descriptions are visible
				ok(gm$(this).is(":visible"), "Gene description should be visible")
			});
			nextTestCase();
		});
	});
	
	testCase("Expand none in the genes tab", function(){
		runDefaultQuery(function(){
			gm$("a[href='#genes_tab']").click();
			gm$("#genes_tab_expand_all").click();
			gm$("#genes_tab_expand_none").click();
			
			gm$(".gene_valid_true .text").each(function(){		// Check that all gene descriptions are not visible
				ok(!gm$(this).is(":visible"), "Gene description should be not visible")
			});
			nextTestCase();
		});
	});
	
	testCase("Functions tab", function(){
		runDefaultQuery(function(){
			gm$("a[href='#go_tab']").click();
			ok(gm$("#go_tab").is(":visible"), "Functions tab should be visible");
			nextTestCase();
		});
	});
	
	testCase("Functions tab allows 7 colours to be used", function(){
		runDefaultQuery(function(){
			gm$("a[href='#go_tab']").click();
			ok(!gm$(".ui-state-disabled.button").is(":visible"), "The buttons should be enabled to click");
			chooseSevenFunctions();
			ok(gm$(".ui-state-disabled.button").is(":visible"), "The buttons should be disabled after 7 are clicked");
			nextTestCase();
		});
	});
	
	testCase("Query genes have a separate colour", function(){
		runDefaultQuery(function(){
			gm$("a[href='#go_tab']").click();
			chooseSevenFunctions();		
			var colours = [];
			gm$("#go_tab > .header > .colouring").each(function(i){ // Save each colour in an array
				colours[i] = gm$(this).attr('colour0');
			});
			
			var pass = true;
			for (var i = 0; i < 8; i++){ // Compare each pair of colours to make sure they are all different
				for (var j = i+1; j < 8; j++){
					if(colours[i] == colours[j]){
						ok(false, "There are two functions with the same colour, " + colours[i]);
						pass = false;
					}
				}
			}
			ok(gm$(".colouring.query").attr("colour0") == "#e0e0e0", "'query genes' has a separate colour, gray");
			ok(pass, "All colours (including query genes) should be different");
			nextTestCase();
		});
	});
	
	testCase("Remove icon for functions tab", function(){
		runDefaultQuery(function(){
			gm$("a[href='#go_tab']").click();
			chooseSevenFunctions();
			ok(!gm$(".ui-icon-minus").is(':visible'), "Remove icon should not be visible yet");
			for (var i = 1; i <= 7; i++){ // Hover over each colour
				gm$(".colouring:nth-child(" + i + ")").mouseover();
				ok(gm$(".colouring:nth-child(" + i + ") .button > .ui-icon-minus").is(':visible'), "Remove icon should be visible after mouseover");
				gm$(".colouring:nth-child(" + i + ")").mouseout();
				ok(!gm$(".colouring:nth-child(" + i + ") .button > .ui-icon-minus").is(':visible'), "Remove icon should not be visible after mouseout");
			}
	
			// Click the last one to ensure it removes the function
			var removed_colour = gm$(".colouring:nth-child(7)").attr("colour0");
			gm$(".colouring:nth-child(7)").mouseover();
			gm$(".colouring:nth-child(7) .button > .ui-icon-minus").click();
			same(gm$(".colouring").size(), 7, "There should only be 6 colours left (plus query genes makes 7)");
			
			for (var i = 1; i <= 6; i++){ // Check that the right colour was removed
				if (gm$(".colouring:nth-child(" + i + ")").attr("colour0") == removed_colour)
					ok(false, "The wrong colour was removed, or the colour was not removed at all");
			}
			nextTestCase();
		});
	});
	
	testCase("Functions legend colours are synchronized with functions tab", function(){
		runDefaultQuery(function(){
			gm$("#menu_go_legend").click();
			gm$("a[href='#go_tab']").click();
			chooseSevenFunctions();
			
			var colours = [];
			gm$("#go_tab > .header > .colouring").each(function(i){ // Save each colour in an array
				colours[i] = gm$(this).attr('colour0');
			});
			var legend_colours = [];
			gm$(".legend_square").each(function(i){ // Get each colour of the legend in a second array
				legend_colours[i] = rgbToHex(gm$(this).css('background-color'));
			});
			
			// Compare the two arrays
			for (var i = 0; i < colours.length; i++){
				for (var j = 0; j < legend_colours.length; j++){
					if (colours[i] == legend_colours[j]){
						ok(true, colours[i] + " was matched in the functions legend");
						break;
					}
					else if (j == legend_colours[i].length){
						ok(false, colours[i] + " was not matched in the functions legend");
					}
				}
			}
			nextTestCase();
		});
	});
	
	testCase("Added colours should be visible in genes tab", function(){
		runDefaultQuery(function(){
			gm$("a[href='#go_tab']").click();
			chooseSevenFunctions();
			
			var colours = [];
			gm$("#go_tab > .header > .colouring").each(function(i){ // Save each colour in an array
				colours[i] = gm$(this).attr('colour0');
			});
			
			// Open genes tab and expand all; then check that the colours are visible
			gm$("a[href='#genes_tab']").click();
			gm$("#genes_tab_expand_all").click();
			
			if (gm$(".colour.coloured").is(":visible")){ // The colours exist; check that all of them exist
				var colours_matched = [];
				gm$(".colour.coloured").each(function(){
					for (var i = 0; i < 7; i++){
						if (rgbToHex(gm$(this).css("background-color")) == colours[i]){
							colours_matched[i] = true;
							break;
						}
						else if (i == 6){
							ok (false, "A colour exists that does not match what was selected");
						}
					}
				});
				for (var i = 0; i < 7; i++){
					ok(colours_matched[i], colours[i] + " should be visible in the genes tab");
				}
			}
			else{
				ok(false, "The colours are not visible in the genes tab, or the expanding did not work");
			}
			nextTestCase();
		});
	});
	
	testModule("Results Page - Advanced Options");
	////#############################################################################
	////RESULTS PAGE -- ADVANCED OPTIONS TESTS
	////#############################################################################
	
	testCase("Check default settings", function(){
		setGenes("pcna", function(){		
			runQuery(function(){
				openAdvancedOptions(function(){
					// Check for Network Weighting buttons
					ok(gm$(".network_weighting_group").is(':visible'), "Network weighting buttons exist");
					// Check that the default is "Automatic weighting"
					ok(gm$("#weighting_AUTOMATIC_SELECT").is(':checked'), "Automatic weighting button should be checked by default");
					// Check for Number of Gene Results option
					ok(gm$("#threshold").is(':visible'), "Number of gene results option exists");
					// Check that the default number of genes returned is 20
					same(gm$("#threshold").val(), "20", "Default number of genes returned should be 20");
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Show/hide advanced options", function(){
		setGenes("pcna", function(){
			runQuery(function(){
				openAdvancedOptions(function(){
					ok(gm$("#networks_section").is(':visible'), "Advanced options should be visible");
					closeAdvancedOptions();
					ok(!gm$("#networks_section").is(':visible'), "Advanced options should be hidden");
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Select all networks", function(){
		runDefaultQuery(function(){
			openAdvancedOptions(function(){
				selectAndCheckAllNetworks();
				wait(function(){
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Deselect all networks", function(){
		runDefaultQuery(function(){		
			openAdvancedOptions(function(){				
				deselectAndCheckAllNetworks();
				wait(function(){
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Select default networks", function(){
		runDefaultQuery(function(){
			openAdvancedOptions(function(){
				selectAndCheckDefaultNetworks();
				wait(function(){
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Sort network by first author", function(){
		runDefaultQuery(function(){
			sortAndCheckNetworkByFirstAuthor(function(){
				nextTestCase();
			});
		});
	});
	
	testCase("Sort network by last author", function(){
		runDefaultQuery(function(){
			sortAndCheckNetworkByLastAuthor(function(){
				nextTestCase();
			});
		});
	});
	testCase("Sort network by size", function(){
		runDefaultQuery(function(){
			sortAndCheckNetworkBySize(function(){
				nextTestCase();
			});
		});
	});
	
	testCase("Sort network by publication date", function(){
		runDefaultQuery(function(){	
			sortAndCheckNetworkByDate(function(){
				nextTestCase();
			});
		});
	});
	
	testCase("Unselect one network and run query", function(){
		openAdvancedOptions(function(){
			// Click on the first checked default network
			var first_default_network_id = gm$("input[type='checkbox'][checked='true'][class='query_network_checkbox']:first").attr('id');
			gm$("input[type='checkbox'][checked='true'][class='query_network_checkbox']:first").click();
			runSpecificDefaultQuery(DEFAULT_ID, function(){
				openAdvancedOptions(function(){
					// Run a default query and verify that it is still unchecked
					ok(!gm$('#' + first_default_network_id).attr('checked'), "Network should be still unselected");
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Equal network weighting", function(){
		openAdvancedOptions(function(){
			// Select equal network weighting
			gm$("#weighting_AVERAGE").click();
			runSpecificDefaultQuery(DEFAULT_ID, function(){
				var percent = gm$(".checktree_network:first .per_cent_text").text();
				// Run default query and verify each network has the same weight
				gm$(".checktree_network .per_cent_text").each(function(i){
					ok(gm$(this).text() == percent, "All weights should be equal");
				});
				nextTestCase();
			});
		});
	});
	
	testCase("Select none and then select default networks", function(){
		runDefaultQuery(function(){
			openAdvancedOptions(function(){
				// Select "none"
				gm$("#network_selection_select_none").trigger('click');
				wait(function(){
					selectAndCheckDefaultNetworks();
					wait(function(){
						nextTestCase();
					});
				});
			});
		});
	});
	
	testModule("Results Page - Cytoscape Interaction");
	////#############################################################################
	////RESULTS PAGE -- CYTOSCAPE INTERACTION TESTS
	////#############################################################################
	
	testCase("Recompute layout", function(){
		runDefaultQuery(function(){
			var nodes = vis().nodes();
			vis().addListener("layout", function(evt) {
				// We need at least 2 nodes in the network!!!
				var n1 = vis().node(nodes[0].data.id);
				var n2 = vis().node(nodes[1].data.id);
				ok( n1.x !== nodes[0].x || n1.y !== nodes[0].y || n2.x !== nodes[1].x || n2.y !== nodes[1].y, "At least one node should have a different position" );
				nextTestCase();
			});
			clickMenuItem("#menu_reset_layout");	
		});
	});
	
	testCase("Merge links", function(){
		runDefaultQuery(function(){
			ok(!vis().edgesMerged(), "Edges unmerged by default");
			clickMenuItem("#menu_merge_links", function() {
				ok(vis().edgesMerged(), "Edges merged now");
				clickMenuItem("#menu_merge_links", function(){
					ok(!vis().edgesMerged(), "Edges unmerged again");
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Highlight neighbors", function(){
		runDefaultQuery(function(){
			var selected_id = gm$("#genes_tab .checktree li:first").attr("gene");
			//var selected_node = vis().node(selected_id);
			if (selected_id == undefined){
				ok(false, "Error: There are no genes, or the selector failed");
				nextTestCase();
			}
			else{
				gm$("a[href='#go_tab']").click();
		
				vis().addListener("select", "nodes", function(evt) {
					clickMenuItem("#menu_neighbors", function() {
						var opacity = vis().node(selected_id).opacity;
						var allNodes = vis().nodes();
						var pass = false;
						
						$.each(allNodes, function(i, n) { // Iterate through every node and check if it is less opaque than the selected node; if at least one is, then pass
							if (n.opacity < opacity) {
								pass = true;
								return false; // this is just to break from the .each
							}
						});
						ok(pass, "At least one node should be more transparent than the selected node");
						nextTestCase();
					});
				});
		
				gm$("#genes_tab .checktree li:first .label").click(); // Highlight the first gene in the genes tab
			}
		});
	});
	
	testCase("Show/hide labels", function(){
		runDefaultQuery(function(){
			// Check that labels are shown by default
			ok(vis().nodeLabelsVisible(), "Labels should be shown by default");
			// Hide labels
			clickMenuItem("#menu_show_labels", function(){
				// Check that labels are hidden
				ok(!vis().nodeLabelsVisible(), "Labels should be hidden now");
				// Show labels
				clickMenuItem("#menu_show_labels", function(){
					// Check that labels are shown again
					ok(vis().nodeLabelsVisible(), "Labels should be shown again");
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Show/hide pan zoom control", function(){
		runDefaultQuery(function(){
			// Check that pan zoom control is shown by default
			ok(vis().panZoomControlVisible(), "Pan zoom control should be shown by default");
			// Hide pan zoom control
			clickMenuItem("#menu_show_panzoom", function(){
				// Check that pan zoom control is hidden
				ok(!vis().panZoomControlVisible(), "Pan zoom control should be hidden now");
				// Show pan zoom control
				clickMenuItem("#menu_show_panzoom", function(){
					// Check that pan zoom control is shown again
					ok(vis().panZoomControlVisible(), "Pan zoom control should be shown again");
					nextTestCase();
				});
			});
		});
	});
	
	testCase("Gene weighting is shown by node sizes", function(){
		runDefaultQuery(function(){
			// Iterate through each result gene
			var prev_gene_id = "default (first gene)";
			var prev_gene_score = 9999999;
			var prev_node_size = 9999999;
			var prev_gene_name = "default (first gene)";
			gm$(".source_false.gene.gene_valid_true").each(function(){
				// Check that the related node is smaller than the previous one
				var current_gene_id = gm$(this).attr("gene");
				var current_gene_score = gm$(this).children(".label").children(".score_text").text();
				var current_gene_name = gm$(this).children('.label').children('.gene_name').text();
				
				try{ // If for some reason the node for the gene does not exist, this takes care of that
					var current_node_size = vis().node(current_gene_id).size;
				}
				catch(err){
					ok(false, err);
				}
				
				if (prev_gene_score == current_gene_score){ // If scores are equal, check that the node sizes are approximately the same
					//console.log(prev_gene_score + " == " + current_gene_score);
					ok(Math.abs(current_node_size - prev_node_size) <= 0.4, "Genes " + current_gene_name + " and " + prev_gene_name + " have equal scores; node size should be within 0.4 (it is " + Math.abs(current_node_size - prev_node_size) + ")");
				}
				else{ // Otherwise, check that it is smaller than the previous one, since the list we are iterating through is sorted by weight
					//console.log(prev_gene_score + " != " + current_gene_score);
					ok(current_node_size <= prev_node_size, "Gene " + current_gene_name + " is less than or equal to " + prev_gene_name);
				}
				//console.log("prev size = " + prev_node_size + "/" + prev_gene_score + ", current size = " + current_node_size + "/" + current_gene_score);
				prev_gene_id = current_gene_id;
				prev_gene_score = current_gene_score;
				prev_node_size = current_node_size;
				prev_gene_name = current_gene_name;
			});
			nextTestCase();
		});
	});
	
	testCase("Selecting a gene highlights itself as well as corresponding node", function(){
		runDefaultQuery(function(){
			var gene_id = gm$("#genes_tab .checktree li:first").attr('gene');
			
			same(gm$(".source_true.selected").length, 0, "No genes should be selected initially"); 
			same(vis().selected().length, 0, "No nodes should be selected initially");
	
			gm$("a[href='#genes_tab']").click();
			gm$("#genes_tab .checktree li:first .label").click();
			
			same(gm$(".source_true.selected").length, 1, "One gene should be selected after clicking the gene");
			same(vis().selected().length, 1, "One node should be selected after clicking the gene");
			
			same(gm$(".source_true.selected").parent().attr('gene'), gene_id, "The correct gene should be highlighted");
			same(vis().selected()[0].data.id, gene_id, "The correct node should be selected");
			nextTestCase();
		});
	});	
	
	testCase("Enable all and none in networks tab shows and hides edges in cytoscape", function(){
		runDefaultQuery(function(){
			wait(function(){
				ok(allEdgesVisible(true), "All edges should be visible by default");
				ok(allNetworkCheckboxesAreChecked(true), "All network checkboxes are checked initially");
				gm$("#networks_tab_check_none").click();
				ok(allEdgesVisible(false), "All edges should be invisible after disabling networks");
				ok(allNetworkCheckboxesAreChecked(false), "All network checkboxes are unchecked after disabling networks");
	 			gm$("#networks_tab_check_all").click();
				ok(allEdgesVisible(true), "All edges should be visible again after enabling networks");
				ok(allNetworkCheckboxesAreChecked(true), "All network checkboxes are checked again after enabling networks");
				nextTestCase();
			}, 4000); // Wait 4 seconds to load flash edges since 1 second seems to be not enough.. not sure how it will be on slower computers
		});
	});
	
	testCase("Deselect individual networks", function(){
		runDefaultQuery(function(){
			wait(function(){
				// Randomly select an edge, and verify that it is visible
				var random_edge_index = Math.ceil(Math.random() * vis().edges().length);
				ok(vis().edges()[random_edge_index].visible, "The selected edge, index " + random_edge_index + ", should be visible initially");
				// Get an array of network names that you need to deselect
				var network_array = vis().edges()[random_edge_index].data.networkNames;
				// Deselect all of the appropriate networks using the array of network names
				for (var i = 0; i < network_array.length; i++){
					deselectNetworkByName(network_array[i]);
				}
				// Verify that the edge is not visible after doing the above steps
				ok(!vis().edges()[random_edge_index].visible, "The selected edge should not be visible after deselecting its networks");
				nextTestCase();
			}, 4000);
		});
	});
	
	testCase("Hovering over functions highlights nodes", function(){
		runDefaultQuery(function(){
			gm$("a[href='#go_tab']").click();
			wait(function(){
				var old_colors = [];
				// Save an array of all the initial node colors
				for (var i = 0; i < vis().nodes().length; i++){
					old_colors[i] = vis().nodes()[i].color;
				}
				
				// Highlight a random function by hovering over it
				var rand = Math.ceil(Math.random() * gm$("#go_table > tbody > tr").size()); // Pick a random function
				var function_name = removeWhiteSpace(gm$("#go_table > tbody > tr:nth-child(" + rand + ")").children("td[class = 'annotation']").text())
				ok(true, "Random function chosen is " + function_name);
				//console.log(gm$("#go_table > tbody > tr:nth-child(" + rand + ")").children("td[class = 'annotation']").text());
				var coverage = parseFloat(gm$("#go_table > tbody > tr:nth-child(" + rand + ")").children("td[class = 'coverage']").attr('value'));
				//console.log(coverage);
				gm$("#go_table > tbody > tr > td[name='" + function_name + "']").mouseover();
				wait(function(){
					// Compare the current node colors to the initial node colors, counting which ones are highlighted and therefore different
					var different_nodes = 0;
					for (var i = 0; i < vis().nodes().length; i++){
						if (old_colors[i] != vis().nodes()[i].color){
							different_nodes++;
						}
					}
					
					// The number of highlighted nodes should match the coverage number
					same(different_nodes, coverage, "A total of " + coverage + " nodes should be highlighted when mouse hovers over the function " + function_name);
					nextTestCase();
				});
			}, 4000);
		});
	});
	
	testModule("Upload Network");
	////#############################################################################
	////UPLOAD NETWORK TESTS
	////#############################################################################
	
	testCase("Upload network help", function(){
		openAdvancedOptions(function(){
			ok(gm$("#uploadArea").is(":visible") && gm$("#uploadHelpBtn").is(":visible"), "Upload link and upload help buttons should be visible initially");
			ok(!gm$("#uploadHelpDialog").is(":visible"), "Upload network help should be hidden initially");
			gm$("#uploadHelpBtn").click();
			ok(gm$("#uploadHelpDialog").is(":visible"), "Upload network help should be visible after clicking the link");
			gm$("span.ui-button-text").click();
			ok(!gm$("#uploadHelpDialog").is(":visible"), "Upload network help should be hidden again after clicking the button");
			nextTestCase();
		});
	});
	
	testCase("Upload a human .txt network", function(){
		var network_name = "Human.txt";
		uploadNetwork("/TxtNetworks/Human.txt", network_name, function(){
			checkUploadValid(network_name);
			nextTestCase();
		});
	});
	
	testCase("Upload an arabidopsis .txt network", function(){
		setOrganismById(ARABIDOPSIS_ID, function(){
			var network_name = "Arabidopsis_co-exp_high.txt";
			uploadNetwork("/TxtNetworks/Arabidopsis_co-exp_high.txt", network_name, function(){
				checkUploadValid(network_name);
				nextTestCase();
			});
		});
	});
	
	testCase("Upload a yeast .txt network", function(){
		setOrganismById(YEAST_ID, function(){
			var network_name = "TAP_core.txt";
			uploadNetwork("/TxtNetworks/TAP_core.txt", network_name, function(){
				checkUploadValid(network_name);
				nextTestCase();
			});
		});
	});
	
	testCase("Trash icon deletes uploaded network", function(){
		var network_name = "Human.txt";
		removeAllUploadedNetworks();
		uploadNetwork("/TxtNetworks/Human.txt", network_name, function(){
			checkUploadValid(network_name);
			removeAllUploadedNetworks();
			wait(function(){
				ok(!gm$("label:contains('" + network_name + "')").is(":visible"), "Uploaded network '" + network_name + "' should not be visible after clicking the trash icon");
				nextTestCase();
			}, 2000);
		});
	});
	
	testCase("Only possible to upload one network at a time", function(){
		getNetwork(gm$("html").attr("contextpath") + "/test/Networks/TxtNetworks/Human.txt", function(file_as_text){
			var file_name = "Human.txt";
			openAdvancedOptions(function(){
				document.getElementById('genemania_iframe').contentWindow.jsUpload(file_as_text, file_name);
				ok(gm$("#uploadDisabler").is(":visible"), "The upload button should have a layer over it, preventing it to be clicked");
				if (gm$("#uploadDisabler").is(":visible")){
					// This wait is put in so that two uploads will not be simultaneously happening
					// if another upload test occurs soon after; that would cause an exception
					waitForNotVisible(gm$("#uploadDisabler"), function(){
						nextTestCase();
					}, 100000);
				}
				else
				{
					nextTestCase();
				}
			});
		});
	});

	testCase("Uploaded network has a description", function(){
		var network_name = "Human.txt";
		openAdvancedOptions(function(){
			// Set to equal network weighting so that the uploaded network will be used regardless
			gm$("#weighting_AVERAGE").click();
			uploadNetwork("/TxtNetworks/Human.txt", network_name, function(){
				// Check that the uploaded network is valid before running query
				if (gm$("label:contains('" + network_name + "'):last").parent().children(".uploadError").is(":visible")){
					ok(false, network_name + " is not a valid network; test aborted");
					nextTestCase();
				}
				else{
					gm$(".query_network:contains('Human.txt')").click();
					ok(gm$(".query_network:contains('Human.txt')").children("[id*='description']").is(":visible"), "Description of uploaded network should be visible");
					gm$(".query_network:contains('Human.txt')").click();
					ok(!gm$(".query_network:contains('Human.txt')").children("[id*='description']").is(":visible"), "Description of uploaded network should not be visible after clicking it again");
					nextTestCase();
				}
			});
		});
	});
	
	testCase("Run query with default networks plus one uploaded network", function(){
		setOrganismById(HUMAN_ID, function(){
			var network_name = "Human.txt";
			openAdvancedOptions(function(){
				// Set to equal network weighting so that the uploaded network will be used regardless
				gm$("#weighting_AVERAGE").click();
				uploadNetwork("/TxtNetworks/Human.txt", network_name, function(){
					// Check that the uploaded network is valid before running query
					if (gm$("label:contains('" + network_name + "'):last").parent().children(".uploadError").is(":visible")){
						ok(false, network_name + " is not a valid network; test aborted");
						nextTestCase();
					}
					else{
						runSpecificDefaultQuery(HUMAN_ID, function(){
							openAdvancedOptions(function(){
								ok(gm$("input[value='Uploaded'][type='checkbox']").is(":checked"), "Uploaded network group in advanced options should be checked by default in the results page");
								ok(gm$(".checktree_top_level:contains('Uploaded')").is(":visible"), "Side panel should show the Uploaded network group");
								nextTestCase();
							});
						});
					}
				});
			});
		});
	});
	
	testCase("Run query with only one uploaded network", function(){
		setOrganismById(HUMAN_ID, function(){
			var network_name = "Human.txt";
			openAdvancedOptions(function(){
				// Deselect all networks
				gm$("#network_selection_select_none").trigger('click');
				uploadNetwork("/TxtNetworks/Human.txt", network_name, function(){
					// Check that the uploaded network is valid before running query
					if (gm$("label:contains('" + network_name + "'):last").parent().children(".uploadError").is(":visible")){
						ok(false, network_name + " is not a valid network; test aborted");
						nextTestCase();
					}
					else{
						runSpecificDefaultQuery(HUMAN_ID, function(){
							openAdvancedOptions(function(){
								ok(gm$("input[value='Uploaded'][type='checkbox']").is(":checked"), "Uploaded network group in advanced options should be checked by default in the results page");
								ok(gm$(".checktree_top_level:contains('Uploaded')").is(":visible"), "Side panel should show the Uploaded network group");
								nextTestCase();
							});
						});
					}
				});
			});
		});
	});
	
//	testCase("Uploaded network has a color", function(){
//		setOrganismById(HUMAN_ID, function(){
//			var network_name = "Human.txt";
//			openAdvancedOptions(function(){
//				// Set to equal network weighting so that the uploaded network will be used regardless
//				gm$("#weighting_AVERAGE").click();
//				uploadNetwork("/TxtNetworks/Human.txt", network_name, function(){
//					// Check that the uploaded network is valid before running query
//					if (gm$("label:contains('" + network_name + "'):last").parent().children(".uploadError").is(":visible")){
//						ok(false, network_name + " is not a valid network; test aborted");
//						nextTestCase();
//					}
//					else{
//						runSpecificDefaultQuery(HUMAN_ID, function(){
//							if (gm$(".checktree_top_level:contains('Uploaded')").is(":visible")){
//								var uploaded_color = rgbToHex(gm$(".checktree_top_level:contains('Uploaded') > .per_cent_bar > .bar").css("background-color"));
//								ok(true, "The color for uploaded networks is " + uploaded_color);
//								var pass = false;
//								for (var i = 0; i < vis().edges().length; i++){
//									if (vis().edges()[i].color == uploaded_color){
//										pass = true;
//										break;
//									}
//								}
//								ok(pass, "The uploaded networks should be shown in the color " + uploaded_color + " in the graph and have color " + );
//								nextTestCase();
//							}
//							else{
//								ok(false, "There is no network group in the side panel of results for the uploaded network");
//								nextTestCase();
//							}
//						});
//					}
//				});
//			});
//		});
//	});
	
	testCase("Upload a network without a header", function(){
		setOrganismById(ARABIDOPSIS_ID, function(){
			var network_name  = "Arabidopsis_NoHeader.txt";
			uploadNetwork("/TxtNetworks/Arabidopsis_NoHeader.txt", network_name, function(){
				checkUploadValid(network_name);
				nextTestCase();
			});
		});
	});
	
	testCase("Upload a network without a score column", function(){
		setOrganismById(ARABIDOPSIS_ID, function(){
			var network_name  = "Arabidopsis_NoScore.txt";
			uploadNetwork("/TxtNetworks/Arabidopsis_NoScore.txt", network_name, function(){
				checkUploadValid(network_name);
				nextTestCase();
			});
		});
	});
	
	testCase("Upload a network that is more than 10% invalid", function(){
		var network_name = "10_percent_invalid.txt";
		uploadNetwork("/TxtNetworks/10_percent_invalid.txt", network_name, function(){
			checkUploadInvalid(network_name);
			nextTestCase();
		});
	});
	
	testCase("Upload an arabidopsis network when the selected organism is human", function(){
		var network_name = "Arabidopsis_co-exp_high.txt";
		uploadNetwork("/TxtNetworks/Arabidopsis_co-exp_high.txt", network_name, function(){
			checkUploadInvalid(network_name);
			nextTestCase();
		});
	});

	
	testModule("Report");
	////#############################################################################
	////REPORT TESTS
	////#############################################################################
	
	testCase("Create default genes report", function(){
		runDefaultQuery(function(){
			var report_items = new Object();
			report_items = saveReportItems(report_items);
			
			$("#genemania iframe").one("load", function(){
				if( gm$("#print_page").size() > 0 ){
					verifyReportItems(report_items);
					nextTestCase();
				}
				else{
					ok(false, "A page other than the report page was loaded");
					nextTestCase();
				}
			});
			
			// Create the report
			wait(function(){
				report(function(){});
			});
		});
	});
	
	testCase("Create a report with non-default search parameters", function(){
		setOrganismById(YEAST_ID, function(){
			setGenes("ade5,7\n" +
					"arg5,6\n" +
					"dur1,2", function(){
				
				selectOneRandomNetwork(function(network_id){
					var random_network_name = gm$(".query_network > input[value="+ network_id +"]").parent().children('label').text();
					if (random_network_name == undefined){
						ok(false, "An error occurred when trying to select a random network to include in the query");
						nextTestCase();
					}
					else{
						wait(function(){
							runQuery(function(){
								var report_items = new Object();
								report_items = saveReportItems(report_items);
								
								$("#genemania iframe").one("load", function(){
									if( gm$("#print_page").size() > 0 ){
										verifyReportItems(report_items);
										nextTestCase();
									}
								});
								
								wait(function(){
									report(function(){});
								});
							});
						});
					}
				});
			});
		});
	});
	
	// Remove print button and checkboxes (do the same thing)
	testCase("Remove items from report", function(){
		runDefaultQuery(function(){
			var report_items = new Object();
			report_items = saveReportItems(report_items);
			// Remove Genes from report items which are verified
			report_items.q_functions = undefined;
			
			$("#genemania iframe").one("load", function(){
					if( gm$("#print_page").size() > 0 ){
						// Click "Remove from print" button at Functions section
						gm$("h2:contains('Functions')").children('.print.button').click();
						ok(!gm$("#go_table").is(":visible"), "Functions section should be hidden after clicking 'Remove from print' button");
						wait(function(){
							// Verify everything besides the functions still exist
							verifyReportItems(report_items);
							nextTestCase();
						});
					}
					else{
						ok(false, "A page other than the report page was loaded");
						nextTestCase();
					}
			});
			
			// Create the report
			wait(function(){
				report(function(){});
			});
		});
	});
});

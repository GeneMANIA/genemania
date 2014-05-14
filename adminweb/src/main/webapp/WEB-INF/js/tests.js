test( "test testframework", function() {
	ok( 1 == "1", "Passed!" );
});

asyncTest( "get organism 1 tree", function() {
	organism_id = 1;
	$.ajax({
		cache: false,
		success: function(data, status, jqXHR){
			expect(2)
			equal(data.length, 4, "4 nodes for organism")
			equal(data[0].title, "Identifiers", "first node is identifeirs");
			start();
		},
		error: function(jqXHR, status, error) {
			ok(false);
			start();
		},
		dataType: "json",
		data: {id: organism_id},
		type: "GET",
		url: "organism/" + organism_id,
	});
});

asyncTest( "get all organisms tree", function() {
	$.ajax({
		cache: false,
		success: function(data, status, jqXHR){
			expect(2);
			equal(data.length, 8, "all organisms returned");
			equal(data[0].title, "Arabidopsis thaliana", "first organism is plant");
			start();
		},
		error: function(jqXHR, status, error) {
			ok(false);
			start();
		},
		dataType: "json",
		type: "GET",
		url: "organism/all",
	});
});
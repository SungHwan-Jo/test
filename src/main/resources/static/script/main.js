
$(document).ready(function(){
  $("#sleep p").hide();
  $("#oom p").hide();
  $("#session p").hide();
  $("#session table").hide();

  $("#sleep").click(function(){
    $("#sleep p").toggle(
      function(){$("#sleep p").addClass('hide')},
      function(){$("#sleep p").addClass('show')}
    )
  });

  $("#oom").click(function(){
    $("#oom p").toggle(
      function(){$("#oom p").addClass('hide')},
      function(){$("#oom p").addClass('show')}
    )
  });

  $("#session").click(function(){
    $("#session p").toggle(
      function(){$("#session p").addClass('hide')},
      function(){$("#session p").addClass('show')}
    )
    $("#session table").toggle(
      function(){$("#session table").addClass('hide')},
      function(){$("#session table").addClass('show')}
    )
  });
  $("#class_view").click(function(){
    $("#class p").show();
    $("#class_search").show();
  });
  $("#class_hide").click(function(){
    $("#class p").hide();
    $("#class_search").hide();
    $("#class_result").hide();
  });

  function viewResourceInfo() {
    if(theform.resource.value.trim()=="")
        return;
    theform.submit();
    $("#class_result").addClass('show');
  }
});



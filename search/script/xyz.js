//draw lines
$.jCanvas({
  strokeStyle: "#00a",
  strokeWidth: 1,
  fillStyle: "#00a",
  font: "10pt Arial",
});
$('canvas').drawLine({
  x1: 45, y1: 215,
  x2: 315, y2: 215,
});
$('canvas').drawText({
  x: 300, y: 230, text: "X",
});
//x+
$('canvas').drawLine({
  x1: 210, y1: 215, 
  x2: 210, y2: 210,
});
$('canvas').drawLine({
  x1: 240, y1: 215, 
  x2: 240, y2: 210,
});
$('canvas').drawLine({
  x1: 270, y1: 215, 
  x2: 270, y2: 210,
});

//x-
$('canvas').drawLine({
  x1: 150, y1: 215, 
  x2: 150, y2: 210,
});
$('canvas').drawLine({
  x1: 120, y1: 215, 
  x2: 120, y2: 210,
});
$('canvas').drawLine({
  x1: 90, y1: 215, 
  x2: 90, y2: 210,
});

$.jCanvas();

//z
$.jCanvas({
  strokeStyle: "#0a2",
  strokeWidth: 1,
  fillStyle: "#0a2",
  font: "10pt Arial",
});
$('canvas').drawLine({
  x1: 180, y1: 80,
  x2: 180, y2: 370,
});
$('canvas').drawText({
  x: 180, y: 60,
  text: "Z",
});
//z+
$('canvas').drawLine({
  x1: 180, y1: 185, 
  x2: 185, y2: 185,
});
$('canvas').drawLine({
  x1: 180, y1: 155, 
  x2: 185, y2: 155,
});
$('canvas').drawLine({
  x1: 180, y1: 125, 
  x2: 185, y2: 125,
});

//z-
$('canvas').drawLine({
  x1: 180, y1: 245, 
  x2: 185, y2: 245,
});
$('canvas').drawLine({
  x1: 180, y1: 275, 
  x2: 185, y2: 275,
});
$('canvas').drawLine({
  x1: 180, y1: 305, 
  x2: 185, y2: 305,
});
$.jCanvas();

//y
$.jCanvas({
  strokeStyle: "#a00",
  strokeWidth: 1,
  fillStyle: "#a00",
  font: "10pt Arial",
});
$('canvas').drawLine({
  x1:283, y1:112,
  x2:77, y2:318,
});
$('canvas').drawText({
  x: 60, y: 340,
  text: "Y",
});
//y+
$('canvas').drawLine({
  x1: 169,  y1: 226, 
  x2: 169,  y2: 222,
});
$('canvas').drawLine({
  x1: 158, y1: 237, 
  x2: 158, y2: 234,
});
$('canvas').drawLine({
  x1: 147, y1: 248,
  x2: 147, y2: 244,
});

//y-
$('canvas').drawLine({
  x1: 191,  y1: 204, 
  x2: 191,  y2: 200,
});
$('canvas').drawLine({
  x1: 202, y1: 193, 
  x2: 202, y2: 189,
});
$('canvas').drawLine({
  x1: 213, y1: 182,
  x2: 213, y2: 178,
});
$.jCanvas();

//draw Tri
$.jCanvas({
  sides: 3,
  radius: 6,
});
$('canvas').drawPolygon({
  x: 318, y: 215, rotate: 90, fillStyle:"#00a",
});
$('canvas').drawPolygon({
  x: 180, y: 77, rotate: 0, fillStyle:"#0a2",
});
$('canvas').drawPolygon({
  x: 74, y: 320, rotate: 225, fillStyle:"#a00",
});
$.jCanvas();


// Draw a circle
$.jCanvas({
  radius: 2,
  fillStyle: "black",
});
$("canvas").drawArc({
  x: 188, y: 147,
});
$("canvas").drawArc({
  x: 229, y: 136,
});
$("canvas").drawArc({
  x: 199, y: 197,
});
$("canvas").drawArc({
  radius: 3,
  x: 218, y: 177,
});
$.jCanvas();
//
$.jCanvas({
  strokeStyle: "#777",
  strokeWidth: 1,
});
$("canvas").drawLine({
  x1: 199, y1: 197,
  x2: 199, y2: 227,
});
$("canvas").drawLine({
  x1: 180, y1: 215,
  x2: 199, y2: 227,
});
$("canvas").drawLine({
  x1: 199, y1: 197,
  x2: 180, y2: 185,
});
/*
$("canvas").drawLine({
  x1: 199, y1: 227,
  x2: 210, y2: 215,
});
$("canvas").drawLine({
  x1: 199, y1: 227,
  x2: 169, y2: 226,
});*/

$("canvas").drawLine({
  x1: 229, y1: 136,
  x2: 229, y2: 226,
});
$("canvas").drawLine({
  x1: 180, y1: 215,
  x2: 229, y2: 226,
});
$("canvas").drawLine({
  x1: 229, y1: 136,
  x2: 180, y2: 125,
});
/*
$("canvas").drawLine({
  x1: 229, y1: 226,
  x2: 240, y2: 215,
});
$("canvas").drawLine({
  x1: 229, y1: 226,
  x2: 169, y2: 226,
});*/

$("canvas").drawLine({
  x1: 188, y1: 147,
  x2: 188, y2: 237,
});
$("canvas").drawLine({
  x1: 180, y1: 215,
  x2: 188, y2: 237,
});
$("canvas").drawLine({
  x1: 188, y1: 147,
  x2: 180, y2: 125,
});
/*
$("canvas").drawLine({
  x1: 188, y1: 237,
  x2: 210, y2: 215,
});
$("canvas").drawLine({
  x1: 188, y1: 237,
  x2: 158, y2: 237,
});*/
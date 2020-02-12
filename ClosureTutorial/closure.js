/*
    For loop async example
*/
function closure(i) {
    // BECAUSE IT PRESERVE THE VALUE OUTSIDE OF THE CLOSURE
    setTimeout(() => {
        console.log(i)
    }, 1000)
}

function exampleOne() {
    for(var i = 0 ; i< 3; i++) {
        closure(i)
    }
}

exampleOne()



/*
    API Example Solution
*/
function getAPI(cb) {
    setTimeout(() => cb("a"), 3000)
}

function getAPIB(cb) {
    setTimeout(() => cb("b"), 2000)
}

function getAPIC(cb) {
    setTimeout(() => cb("c"), 1000)
}


function aggregateValue(cb) {
    var i = 0 // you can still define the value here
    var arr = []

    function callback(value) {
        arr = [...arr, value];
        if (i < 2) {
            i++;
        } else {
            cb(arr)
        }
    }
    

    getAPI(callback)
    getAPIB(callback)
    getAPIC(callback)
}

// aggregateValue((arr) => console.log(`aggregate value ${arr}`))

// testAsync()



/*
    Simple Closure definition
*/
function takeOne() {
    let i = 0;
    return function incrementFunction() {
        return i++;
    }
}

let func = takeOne()
// console.log(func())
// console.log(func())
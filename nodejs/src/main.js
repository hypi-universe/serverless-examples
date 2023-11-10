/**
 * @param input has env and args
 * @param callback accepts two params, 1st one is the response to return and second is error if there is one
 */
function hellWorld(input, callback) {
        console.log('Yeah, we got some input', input, input.env, input.args);
    callback(input, null);
}

// Must export `main`
exports.main = hellWorld;
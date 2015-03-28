/// <reference path="reference.ts"/>

class HelloWorldCtrl {
	$http: ng.IHttpService;
	name: string;
	greeting: string;

	static $inject = ["$http"];
	constructor($http: ng.IHttpService) {
		this.$http = $http;
		this.name = HelloWorldCtrl.getRandomName();
		this.greeting = HelloWorldCtrl.getRandomGreeting();
	}

	chooseNewGreeting() {
		this.greeting = HelloWorldCtrl.getRandomGreeting();
	}

	submit() {
		this.$http.get("greet", {
			params: {name: this.name, greeting: this.greeting}
		})
		.success((data: GreetingServletResponse, status, headers, config) => {
				window.alert(data.text);
			})
		.error((data, status, headers, config) => {
				console.log(arguments);
				window.alert("It didn't work.");
			});
	}

	static getRandomGreeting() {
		return HelloWorldCtrl.getRandomString([
			"Hello",
			"Hi",
			"Ahoy",
		]);
	}

	static getRandomName() {
		return HelloWorldCtrl.getRandomString([
			"Alice",
			"Bob",
			"Carol",
			"Dave",
		]);
	}

	static getRandomString(strings: string[]) {
		var index = Math.floor(Math.random() * strings.length);
		return strings[index];
	}
}

interface GreetingServletResponse {
	text: string;
}

angular.module("app").controller("HelloWorldCtrl", HelloWorldCtrl);

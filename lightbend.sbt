resolvers in ThisBuild += "lightbend-commercial-mvn" at
        "https://repo.lightbend.com/pass/hS_PDKpX3iOpXyWiczdwm1PR9tCy4MQuO0TtHY7PhWpUZhLT/commercial-releases"
resolvers in ThisBuild += Resolver.url("lightbend-commercial-ivy",
        url("https://repo.lightbend.com/pass/hS_PDKpX3iOpXyWiczdwm1PR9tCy4MQuO0TtHY7PhWpUZhLT/commercial-releases"))(Resolver.ivyStylePatterns)
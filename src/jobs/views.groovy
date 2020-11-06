import jobLib.rhsmLib


def devJobs = [
    rhsmLib.candlepinJobFolder,
    rhsmLib.submanJobFolder,
    'DockerCleanup',
    'WsCleanup'
]

listView("Devel") {
  description('Engineering Jobs')
  filterExecutors()
  jobs {
    devJobs.each { j ->
      name(j)
    }
  }
  columns {
    status()
    weather()
    name()
    lastSuccess()
    lastDuration()
    progressBar()
    buildButton()
  }
}
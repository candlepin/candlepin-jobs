import jobLib.rhsmLib


def candlepinJobs = [
    rhsmLib.candlepinJobFolder,
    rhsmLib.submanJobFolder,
    'Docker Cleanup'
]

listView("Candlepin") {
  description('Candlepin Engineering Jobs')
  filterExecutors()
  jobs {
    candlepinJobs.each { j ->
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
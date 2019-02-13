import jobLib.rhsmLib

folder(rhsmLib.submanJobFolder) {
    description("This is folder for the subscription manager pipeline jobs.")
}

folder(rhsmLib.candlepinJobFolder) {
    description("This is folder for the candlepin pipeline jobs.")
}
